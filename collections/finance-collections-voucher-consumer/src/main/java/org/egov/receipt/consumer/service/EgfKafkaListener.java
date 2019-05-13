package org.egov.receipt.consumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.receipt.consumer.entity.VoucherIntegrationLog;
import org.egov.receipt.consumer.model.ReceiptReq;
import org.egov.receipt.consumer.model.VoucherIntegrationLogTO;
import org.egov.receipt.consumer.model.VoucherResponse;
import org.egov.receipt.consumer.repository.VoucherIntegartionLogRepository;
import org.egov.receipt.custom.exception.VoucherCustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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
	private VoucherIntegrationLogTO voucherIntegrationLogTO;
	
	public static final Logger LOGGER = LoggerFactory.getLogger(EgfKafkaListener.class);
	private static final String RECEIPT_TYPE = "Receipt";
	
	@KafkaListener(topics = "${egov.collection.receipt.voucher.save.topic}")
    public void processToCreateReceiptVoucher(ConsumerRecord<String, String> record) {
		ReceiptReq request = null;
		if(LOGGER.isInfoEnabled())
			LOGGER.info("topic : " + record.topic() + "\t\t" + "value : " + record.value());
        VoucherResponse voucherResponse = null;
        try {
            request = objectMapper.readValue(record.value(), ReceiptReq.class);
            if(LOGGER.isInfoEnabled())
            	LOGGER.info("Recieved reciept request : "+request);
               if (voucherService.isVoucherCreationEnabled(request)) {
            	   voucherResponse = voucherService.createReceiptVoucher(request);
            	   if(LOGGER.isInfoEnabled())
            		   LOGGER.info("voucherResponse : "+voucherResponse);
            	   if(voucherResponse != null){
            		   String status = voucherResponse.getResponseInfo().getStatus();
            		   if(status.equals("201")){
            			   voucherIntegrationLogTO.setStatus("SUCCESS");
            			   voucherIntegrationLogTO.setDescription("Voucher created successfully with voucher number : "+voucherResponse.getVouchers().get(0).getVoucherNumber());
            			   this.getBackupToDB(request);
            			   if(LOGGER.isInfoEnabled())
            				   LOGGER.info("Voucher created successfully with voucher number : "+voucherResponse.getVouchers().get(0).getVoucherNumber());
            		   }
            		   receiptService.updateReceipt(request, voucherResponse);
            		   instrumentService.createInstrument(request, voucherResponse);
            	   }
               }else{
            	   voucherIntegrationLogTO.setStatus("FAILED");
    			   voucherIntegrationLogTO.setDescription("Voucher Creation is not enabled for business service code : "+request.getReceipt().get(0).getBill().get(0).getBillDetails().get(0).getBusinessService());
            	   this.getBackupToDB(request);
    			   if(LOGGER.isInfoEnabled())
            		   LOGGER.info("Voucher Creation is not enabled for business service code : "+request.getReceipt().get(0).getBill().get(0).getBillDetails().get(0).getBusinessService());
               }
        } catch (Exception e) {
        	this.getBackupToDB(request);
        	if(LOGGER.isErrorEnabled())
        		LOGGER.error(e.getMessage());
        }
    }
	@KafkaListener(topics = "${egov.collection.receipt.voucher.cancel.topic}")
    public void processToCancelReceiptVoucher(ConsumerRecord<String, String> record) {
		ReceiptReq request = null;
		if(LOGGER.isInfoEnabled())
			LOGGER.info("topic : " + record.topic() + "\t\t" + "value : " + record.value());
        try {
            request = objectMapper.readValue(record.value(), ReceiptReq.class);
            String voucherNumber = request.getReceipt().get(0).getBill().get(0).getBillDetails().get(0).getVoucherHeader();
            if(LOGGER.isInfoEnabled())
            	LOGGER.info("Recieved reciept request : "+request);
            if(voucherService.isVoucherExists(request)){
            	voucherService.cancelReceiptVoucher(request);
            	instrumentService.cancelInstrument(request.getReceipt().get(0));
            	voucherIntegrationLogTO.setStatus("SUCCESS");
 			   	voucherIntegrationLogTO.setDescription("Voucher number : "+voucherNumber+" is CANCELLED successfully!");
 			   	this.getBackupToDB(request);
            }else{
            	voucherIntegrationLogTO.setStatus("FAILED");
 			   	voucherIntegrationLogTO.setDescription("Voucher is not found for voucherNumber : "+voucherNumber);
            	if(LOGGER.isInfoEnabled())
            		LOGGER.info("Voucher is not found for voucherNumber : "+voucherNumber);
            }
        } catch (Exception e) {
        	if(LOGGER.isErrorEnabled())
        		LOGGER.error(e.getMessage());
        	this.getBackupToDB(request);
        }
    }
	
	/**
	 * function use to take a backup to DB after success/failure of voucher creation process.
	 */
	private void getBackupToDB(ReceiptReq request){
		try {
			VoucherIntegrationLog voucherIntegrationLog = new VoucherIntegrationLog();
			voucherIntegrationLog.setReferenceNumber(request.getReceipt().get(0).getReceiptNumber());
			voucherIntegrationLog.setRequestJson(request.toString());
			voucherIntegrationLog.setStatus(voucherIntegrationLogTO.getStatus());
			voucherIntegrationLog.setVoucherNumber(voucherIntegrationLogTO.getVoucherNumber());
			voucherIntegrationLog.setDescription(voucherIntegrationLogTO.getDescription());
			voucherIntegrationLog.setType(RECEIPT_TYPE);
			voucherIntegartionLogRepository.save(voucherIntegrationLog);			
		} catch (Exception e) {
			LOGGER.error("ERROR occurred while doing a backup to databases. "+e.getCause());
		}
	}
}
