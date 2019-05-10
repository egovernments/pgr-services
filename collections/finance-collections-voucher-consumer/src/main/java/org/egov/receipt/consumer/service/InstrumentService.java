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

import java.util.Collections;
import org.egov.mdms.service.TokenService;
import org.egov.receipt.consumer.model.FinancialStatus;
import org.egov.receipt.consumer.model.Instrument;
import org.egov.receipt.consumer.model.InstrumentContract;
import org.egov.receipt.consumer.model.InstrumentRequest;
import org.egov.receipt.consumer.model.InstrumentResponse;
import org.egov.receipt.consumer.model.InstrumentStatusEnum;
import org.egov.receipt.consumer.model.InstrumentVoucherContract;
import org.egov.receipt.consumer.model.Receipt;
import org.egov.receipt.consumer.model.ReceiptReq;
import org.egov.receipt.consumer.model.RequestInfo;
import org.egov.receipt.consumer.model.VoucherResponse;
import org.egov.reciept.consumer.config.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InstrumentService {

	public static final Logger LOGGER = LoggerFactory.getLogger(InstrumentService.class);

	private static final String FINANCE_STATUS_NEW = "New";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private FinancialStatusService financialStatusService;

	public InstrumentResponse createInstrument(ReceiptReq receiptRequest, VoucherResponse voucherResponse) {
		
		try {
			Receipt receipt = receiptRequest.getReceipt().get(0);
			FinancialStatus status = financialStatusService.getByCode(FINANCE_STATUS_NEW, receipt.getTenantId());
			RequestInfo requestInfo = new RequestInfo();
			requestInfo.setAuthToken(tokenService.generateAdminToken(receipt.getTenantId()));
			
			Instrument instrument = receipt.getInstrument();
			InstrumentContract instrumentContract = instrument.toContract();
			instrumentContract.setFinancialStatus(status);
			if (voucherResponse != null) {
				prepareInstrumentVoucher(instrumentContract, voucherResponse, receipt);
			}
			InstrumentRequest request = new InstrumentRequest();
			request.setInstruments(Collections.singletonList(instrumentContract));
			request.setRequestInfo(requestInfo);
			LOGGER.info("call:" + propertiesManager.getHostUrl() + propertiesManager.getInstrumentCreate());
			return restTemplate.postForObject(
					propertiesManager.getHostUrl() + propertiesManager.getInstrumentCreate(), request,
					InstrumentResponse.class);
			
		} catch (Exception e) {
			LOGGER.error("ERROR occured while creating instrument "+e.getStackTrace());
		}
		return null;
	}

	private void prepareInstrumentVoucher(InstrumentContract instrumentContract, VoucherResponse voucherResponse,
			Receipt receipt) {

		InstrumentVoucherContract ivContract = new InstrumentVoucherContract();
		ivContract.setVoucherHeaderId(voucherResponse.getVouchers().get(0).getVoucherNumber());
		ivContract.setReceiptHeaderId(receipt.getReceiptNumber());
		instrumentContract.setInstrumentVouchers(Collections.singletonList(ivContract));
	}

	public void cancelInstrument(Receipt receipt) {
		InstrumentRequest request = new InstrumentRequest();
		this.setInstrumentRequest(request, receipt);
		LOGGER.info("call:" + propertiesManager.getHostUrl() + propertiesManager.getInstrumentCancel());
		InstrumentResponse postForObject = restTemplate.postForObject(
				propertiesManager.getHostUrl() + propertiesManager.getInstrumentCancel(), request,
				InstrumentResponse.class);
		LOGGER.info("InstrumentResponse :::: " + postForObject);
	}

	private void setInstrumentRequest(InstrumentRequest request, Receipt receipt) {
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setAuthToken(tokenService.generateAdminToken(receipt.getTenantId()));
		Instrument instrument = receipt.getInstrument();
		InstrumentContract instrumentContract = instrument.toContract();
		request.setInstruments(Collections.singletonList(instrumentContract));
		request.setRequestInfo(requestInfo);
	}

}
