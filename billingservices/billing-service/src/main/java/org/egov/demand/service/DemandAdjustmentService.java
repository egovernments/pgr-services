package org.egov.demand.service;

import org.egov.demand.web.contract.BillRequest;
import org.egov.demand.web.contract.Receipt;
import org.egov.demand.web.contract.ReceiptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemandAdjustmentService {
	
	private static final Logger logger = LoggerFactory.getLogger(DemandAdjustmentService.class);

	@Autowired
	private DemandService demandService;
	
	public void adjustDemandOnReceiptCancellation(ReceiptRequest receiptRequest) {
		for(Receipt receipt: receiptRequest.getReceipt()) {
			try {
				BillRequest billRequest = BillRequest.builder().requestInfo(receiptRequest.getRequestInfo()).bills(receipt.getBill()).build();
				//FIXME TOOD update bill from reciept has been commented out
				//demandService.updateDemandFromBill(billRequest, true);
			}catch(Exception e) {
				log.error("Couldn't adjust the demand! Exception: "+e);
				continue;
			}
		}
	}
	
	
	/*The logic of reverting the demand is complicated and appears to be inefficient, we need to revisit the logic and see if it can be made easier. 
	The code snipet commented out below was an attempt towards the same and gives a head start for refactoring the logic, Take this up while refactoring */
	

	
/*	public void adjustDemandOnReceiptCancellation(ReceiptRequest receiptRequest) {
		for(Receipt receipt: receiptRequest.getReceipt()) {
			for(Bill bill: receipt.getBill()) {
				Map<String, Map<String, BigDecimal>> mapOfBillDetailsAndCreditAmount = new HashMap<>();
				for(BillDetail billDetail: bill.getBillDetails()) {
					Map<String, BigDecimal> mapOfAccDetailIdAndCrAmount = billDetail.getBillAccountDetails().parallelStream()
							.collect(Collectors.toMap(BillAccountDetail::getTaxHeadCode, BillAccountDetail::getCreditAmount));
					mapOfBillDetailsAndCreditAmount.put(billDetail.getConsumerCode(), mapOfAccDetailIdAndCrAmount);
					adjustDemand(receiptRequest.getRequestInfo(), receipt.getTenantId(), mapOfBillDetailsAndCreditAmount);
				}
			}
		}
	}
	
	
	public void adjustDemand(RequestInfo requestInfo, String tenantId, Map<String, Map<String, BigDecimal>> mapOfAccDetailIdAndCrAmount) {
		DemandCriteria demandCriteria = DemandCriteria.builder().tenantId(tenantId).consumerCode(mapOfAccDetailIdAndCrAmount.keySet()).build();
		DemandResponse demandResponse = demandService.getDemands(demandCriteria, requestInfo);
		if(demandResponse.getDemands().isEmpty()) {
			log.info("Couldn't fetch demands from the db!");
			return;
		}else {
			for(String consumerCode: mapOfAccDetailIdAndCrAmount.keySet()) {
				Map<String, BigDecimal> mapOfBillDetailsAndCreditAmount = mapOfAccDetailIdAndCrAmount.get(consumerCode);
				List<Demand> demands = demandResponse.getDemands().parallelStream()
						.filter(obj -> obj.getConsumerCode().equals(consumerCode)).collect(Collectors.toList());
				if(demands.size() > 1 || demands.isEmpty()) {
					log.info("There are either no or duplicate demands for this consumercode!");
					continue;
				}else {
					for(DemandDetail detail: demands.get(0).getDemandDetails()) {
						detail.setCollectionAmount(detail.getCollectionAmount().subtract(mapOfBillDetailsAndCreditAmount.get(detail.getTaxHeadMasterCode())));
					}
				}
			}
			DemandRequest demandRequest = DemandRequest.builder().requestInfo(requestInfo).demands(demandResponse.getDemands()).build();
			try {
				demandService.update(demandRequest);
			}catch(Exception e) {
				log.error("Updating demands with cancelled receipts FAILED! Error: "+e);
			}
		}
		
	}*/

}