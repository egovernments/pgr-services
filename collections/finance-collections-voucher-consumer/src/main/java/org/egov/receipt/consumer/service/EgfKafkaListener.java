/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any Long of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.receipt.consumer.service;

import java.util.Date;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.receipt.consumer.entity.VoucherIntegrationLog;
import org.egov.receipt.consumer.model.FinanceMdmsModel;
import org.egov.receipt.consumer.model.ProcessStatus;
import org.egov.receipt.consumer.model.ReceiptReq;
import org.egov.receipt.consumer.model.VoucherResponse;
import org.egov.receipt.consumer.repository.VoucherIntegartionLogRepository;
import org.egov.receipt.custom.exception.VoucherCustomException;
import org.egov.reciept.consumer.config.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EgfKafkaListener {
	@Autowired
    private ObjectMapper objectMapper;
	@Autowired
	private VoucherService voucherService;
	@Autowired
	private ReceiptService receiptService;
	@Autowired
	private InstrumentService instrumentService;
	@Autowired
	private VoucherIntegartionLogRepository voucherIntegartionLogRepository;
	@Autowired
	private PropertiesManager manager;
	
	public static final Logger LOGGER = LoggerFactory.getLogger(EgfKafkaListener.class);
	private static final String RECEIPT_TYPE = "Receipt";
	
	@KafkaListener(topics = {"${egov.collection.receipt.voucher.save.topic}","${egov.collection.receipt.voucher.cancel.topic}"})	
    public void process(ConsumerRecord<String, String> record) {
        VoucherResponse voucherResponse = null;
        String voucherNumber = "";
        ReceiptReq request = null;
        FinanceMdmsModel finSerMdms = new FinanceMdmsModel();
        try {
        	String topic = record.topic();
        	request = objectMapper.readValue(record.value(), ReceiptReq.class);
        	LOGGER.info("topic : {} ,  request : {}", topic, request);
        	if(topic.equals(manager.getVoucherCreateTopic())){
        		if (voucherService.isTenantEnabledInFinanceModule(request, finSerMdms) && voucherService.isVoucherCreationEnabled(request, finSerMdms)) {
        			voucherResponse = voucherService.createReceiptVoucher(request, finSerMdms);
        			voucherNumber = voucherResponse.getVouchers().get(0).getVoucherNumber();
        			receiptService.updateReceipt(request, voucherResponse);
        			instrumentService.createInstrument(request, voucherResponse, finSerMdms);
        			this.getBackupToDB(request,ProcessStatus.SUCCESS,"Voucher created successfully with voucher number : "+voucherNumber,voucherNumber);
        		}else{
        			//Todo : Status should be different
        			this.getBackupToDB(request,ProcessStatus.NA,"Voucher Creation is not enabled for business service code : "+request.getReceipt().get(0).getBill().get(0).getBillDetails().get(0).getBusinessService(),voucherNumber);
        		}
        	}else if(topic.equals(manager.getVoucherCancelTopic())){
                voucherNumber = request.getReceipt().get(0).getBill().get(0).getBillDetails().get(0).getVoucherHeader();
                if(voucherService.isVoucherExists(request)){
                	voucherService.cancelReceiptVoucher(request);
                	instrumentService.cancelInstrument(request,finSerMdms);
     			   	this.getBackupToDB(request,ProcessStatus.SUCCESS,"Voucher number : "+voucherNumber+" is CANCELLED successfully!",voucherNumber);
                }else{
                	this.getBackupToDB(request,ProcessStatus.FAILED,"Voucher is not found for voucherNumber : "+voucherNumber,voucherNumber);
                }
        	}
        }catch(VoucherCustomException e){
        	this.getBackupToDB(request,e.getStatus(),e.getMessage(),voucherNumber);
        	LOGGER.error(e.getMessage());
        }catch (Exception e) {
        	this.getBackupToDB(request,ProcessStatus.FAILED,e.getMessage(),voucherNumber);
       		LOGGER.error(e.getMessage());
        }
    }
	
	/**
	 * function use to take a backup to DB after success/failure of voucher creation process.
	 */
	private void getBackupToDB(ReceiptReq request,ProcessStatus status, String description, String voucherNumber){
		try {
			VoucherIntegrationLog voucherIntegrationLog = new VoucherIntegrationLog();
			voucherIntegrationLog.setStatus(status.name());
			voucherIntegrationLog.setDescription(description);
			this.prepareVoucherIntegrationLog(voucherIntegrationLog, request, voucherNumber);
			voucherIntegartionLogRepository.saveVoucherIntegrationLog(voucherIntegrationLog);			
		} catch (Exception e) {
			LOGGER.error("ERROR occurred while doing a backup to databases. "+e.getMessage());
		}
	}
	
	private void prepareVoucherIntegrationLog(VoucherIntegrationLog voucherIntegrationLog, ReceiptReq request, String voucherNumber){
		voucherIntegrationLog.setReferenceNumber(request.getReceipt().get(0).getBill().get(0).getBillDetails().get(0).getReceiptNumber());
		ObjectMapper mappper = new ObjectMapper();
		try {
			String jsonReq = mappper.writeValueAsString(request);
			voucherIntegrationLog.setRequestJson(jsonReq);
		} catch (JsonProcessingException e) {
			LOGGER.error("ERROR occurred while parsing the ReceiptRequest "+e.getMessage());
		}
		voucherIntegrationLog.setVoucherNumber(voucherNumber);
		voucherIntegrationLog.setType(RECEIPT_TYPE);
		voucherIntegrationLog.setTenantId(request.getReceipt().get(0).getTenantId());
		voucherIntegrationLog.setCreatedDate(new Date());
	}
}
