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
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.demand.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.Bill;
import org.egov.demand.model.BillDetail;
import org.egov.demand.model.CollectedReceipt;
import org.egov.demand.model.ConsolidatedTax;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.DemandDetailCriteria;
import org.egov.demand.model.DemandDue;
import org.egov.demand.model.DemandDueCriteria;
import org.egov.demand.model.DemandUpdateMisRequest;
import org.egov.demand.producer.Producer;
import org.egov.demand.repository.DemandRepository;
import org.egov.demand.repository.ServiceRequestRepository;
import org.egov.demand.util.DemandEnrichmentUtil;
import org.egov.demand.util.SequenceGenService;
import org.egov.demand.web.contract.BillRequest;
import org.egov.demand.web.contract.DemandDetailResponse;
import org.egov.demand.web.contract.DemandDueResponse;
import org.egov.demand.web.contract.DemandRequest;
import org.egov.demand.web.contract.DemandResponse;
import org.egov.demand.web.contract.ReceiptRequest;
import org.egov.demand.web.contract.User;
import org.egov.demand.web.contract.UserResponse;
import org.egov.demand.web.contract.UserSearchRequest;
import org.egov.demand.web.contract.factory.ResponseFactory;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DemandService {

	private static final Logger logger = LoggerFactory.getLogger(DemandService.class);

	@Autowired
	private SequenceGenService sequenceGenService;

	@Autowired
	private DemandRepository demandRepository;

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private ResponseFactory responseInfoFactory;

	@Autowired
	private DemandEnrichmentUtil demandEnrichmentUtil;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private Producer producer;
	
	/**
	 * Method to create new demand 
	 * 
	 * generates ids and saves to the repositroy
	 * 
	 * @param demandRequest
	 * @return
	 */
	public DemandResponse create(DemandRequest demandRequest) {

		logger.info("the demand request in create async : {}", demandRequest);

		RequestInfo requestInfo = demandRequest.getRequestInfo();
		List<Demand> demands = demandRequest.getDemands();
		AuditDetails auditDetail = getAuditDetail(requestInfo);

		generateAndSetIdsForNewDemands(demands, auditDetail);
		save(demandRequest);
		producer.push(applicationProperties.getDemandIndexTopic(), demandRequest);
		return new DemandResponse(responseInfoFactory.getResponseInfo(requestInfo, HttpStatus.CREATED), demands);
	}

	/**
	 * Method to generate and set ids, Audit details to the demand and demand
	 * details object
	 * 
	 * @param demandRequest
	 * @return
	 */
	private void generateAndSetIdsForNewDemands(List<Demand> demands, AuditDetails auditDetail) {

		List<DemandDetail> demandDetails = new ArrayList<>();

		int currentDemandId = 0;
		int demandsSize = demands.size();
		List<String> demandIds = sequenceGenService.getIds(demandsSize, applicationProperties.getDemandSeqName());

		/*
		 * looping demands to set ids and collect demand details in another list
		 */
		for (Demand demand : demands) {

			String demandId = demandIds.get(currentDemandId++);
			demand.setId(demandId);
			demand.setAuditDetails(auditDetail);
			String tenantId = demand.getTenantId();

			for (DemandDetail demandDetail : demand.getDemandDetails()) {
				demandDetail.setDemandId(demandId);
				demandDetail.setTenantId(tenantId);
				demandDetail.setAuditDetails(auditDetail);
				demandDetails.add(demandDetail);
			}
		}

		generateAndsetIdsForDemandDetails(demandDetails);
	}

	/**
	 * Generates and sets ids for the demand details
	 * 
	 * @param demandDetails
	 */
	private void generateAndsetIdsForDemandDetails(List<DemandDetail> demandDetails) {
		
		int demandDetailsSize = demandDetails.size();
		List<String> demandDetailIds = sequenceGenService.getIds(demandDetailsSize,
				applicationProperties.getDemandDetailSeqName());

		int currentDetailId = 0;
		for (DemandDetail demandDetail : demandDetails) {
			if (demandDetail.getCollectionAmount() == null)
				demandDetail.setCollectionAmount(BigDecimal.ZERO);
			demandDetail.setId(demandDetailIds.get(currentDetailId++));
		}
	}
	
	
	/**
	 * Update method for demand flow 
	 * 
	 * updates the existing demands and inserts in case of new
	 * 
	 * @param demandRequest demand request object to be updated
	 * @return
	 */
	public DemandResponse updateAsync(DemandRequest demandRequest) {

		log.debug("the demand service : " + demandRequest);

		RequestInfo requestInfo = demandRequest.getRequestInfo();
		List<Demand> demands = demandRequest.getDemands();
		AuditDetails auditDetail = getAuditDetail(requestInfo);

		List<Demand> newDemands = new ArrayList<>();
		List<DemandDetail> newDemandDetails = new ArrayList<>();

		for (Demand demand : demands) {

			if (demand.getId() == null) {

				newDemands.add(demand);
			} else {

				updateoldDemandandCollectNewDemandDetails(auditDetail, newDemandDetails, demand);
			}
		}

		generateAndSetIdsForNewDemands(newDemands, auditDetail);
		generateAndsetIdsForDemandDetails(newDemandDetails);

		update(demandRequest);
		producer.push(applicationProperties.getDemandIndexTopic(), demandRequest);
		return new DemandResponse(responseInfoFactory.getResponseInfo(requestInfo, HttpStatus.CREATED), demands);
	}

	/**
	 * Collects the new demand details in a list and updates the audit modified
	 * fields for old details
	 * 
	 * @param auditDetail
	 * @param newDmdDetails
	 * @param demand
	 */
	private void updateoldDemandandCollectNewDemandDetails(AuditDetails auditDetail, List<DemandDetail> newDmdDetails,
			Demand demand) {

		demand.setAuditDetails(auditDetail);

		for (DemandDetail demandDetail : demand.getDemandDetails()) {

			/*
			 * if demand detail is new set audit and collect to set ids
			 */
			if (demandDetail.getId() == null) {
				
				demandDetail.setDemandId(demand.getId());
				demandDetail.setAuditDetails(auditDetail);
				newDmdDetails.add(demandDetail);
			} else {
				/*
				 * update audit in case of update only
				 */
				demandDetail.setAuditDetails(auditDetail);
			}
		}
	}
	
	public DemandResponse getDemands(DemandCriteria demandCriteria, RequestInfo requestInfo) {
		
		UserSearchRequest userSearchRequest = null;
		List<User> owners = null;
		List<Demand> demands = null;
		List<CollectedReceipt> receipts=null;
		
		String userUri = applicationProperties.getUserServiceHostName()
				.concat(applicationProperties.getUserServiceSearchPath());
		
		/*
		 * user type is CITIZEN by default because only citizen can have demand or payer can be null
		 */
		String citizenTenantId = demandCriteria.getTenantId().split("\\.")[0];
		
		/*
		 * If payer related data is provided first then user search has to be made first followed by demand search
		 */
		if (demandCriteria.getEmail() != null || demandCriteria.getMobileNumber() != null) {
			
			userSearchRequest = UserSearchRequest.builder().requestInfo(requestInfo)
					.tenantId(citizenTenantId).emailId(demandCriteria.getEmail())
					.mobileNumber(demandCriteria.getMobileNumber()).build();
			
			owners = mapper.convertValue(serviceRequestRepository.fetchResult(userUri, userSearchRequest), UserResponse.class).getUser();
			
			Set<String> ownerIds = owners.stream().map(User::getUuid).collect(Collectors.toSet());
			demands = demandRepository.getDemands(demandCriteria, ownerIds);
			
			/*
			 * sorting demand based on from period
			 */
			demands.sort(Comparator.comparing(Demand::getTaxPeriodFrom));
		} else {
			/*
			 * If no payer related data given then search demand first then enrich payer(user) data
			 */
			demands = demandRepository.getDemands(demandCriteria, null);
			if (!demands.isEmpty()) {
				
				demands.sort(Comparator.comparing(Demand::getTaxPeriodFrom));
				Set<String> payerUuids = demands.stream().filter(demand -> null != demand.getPayer())
						.map(demand -> demand.getPayer().getUuid()).collect(Collectors.toSet());

				if (!CollectionUtils.isEmpty(payerUuids)) {

					userSearchRequest = UserSearchRequest.builder().requestInfo(requestInfo).uuid(payerUuids).build();

					owners = mapper.convertValue(serviceRequestRepository.fetchResult(userUri, userSearchRequest),
							UserResponse.class).getUser();
				}
			}
		}
		if (!CollectionUtils.isEmpty(demands) && !CollectionUtils.isEmpty(owners))
			demands = demandEnrichmentUtil.enrichPayer(demands, owners);
		
		/*
		 * sorting the demand details in demand based on taxheadcode
		 */
		demands.forEach(demand -> demand.getDemandDetails().sort(Comparator.comparing(DemandDetail::getTaxHeadMasterCode)));
		
		return new DemandResponse(responseInfoFactory.getResponseInfo(requestInfo, HttpStatus.OK), demands, receipts);
	}

	public void save(DemandRequest demandRequest) {
		demandRepository.save(demandRequest);
	}

	public void update(DemandRequest demandRequest) {
		demandRepository.update(demandRequest);
	}

	/**
	 * Generates the Audit details object for the requested user and current time
	 * 
	 * @param requestInfo
	 * @return
	 */
	private AuditDetails getAuditDetail(RequestInfo requestInfo) {

		String userId = requestInfo.getUserInfo().getId().toString();
		Long currEpochDate = System.currentTimeMillis();

		return AuditDetails.builder().createdBy(userId).createdTime(currEpochDate).lastModifiedBy(userId)
				.lastModifiedTime(currEpochDate).build();
	}
	
	
	/*
	 * 
	 * 
	 * 
	 * Reciept based methods
	 * 
	 * 
	 * 
	 */
	
	
	public DemandResponse  updateDemandFromReceipt(ReceiptRequest receiptRequest, org.egov.demand.model.BillDetail.StatusEnum status)
	{
	    BillRequest billRequest=new BillRequest();
	    if(receiptRequest !=null && receiptRequest.getReceipt() !=null && !receiptRequest.getReceipt().isEmpty()){
	    billRequest.setRequestInfo(receiptRequest.getRequestInfo());
	    List<Bill> bills=receiptRequest.getReceipt().get(0).getBill();
	    for(Bill bill:bills){
	    	for(BillDetail billDetail: bill.getBillDetails())
	    		billDetail.setStatus(status);
	    }
	    billRequest.setBills(bills);
	    }
	    return null; //updateDemandFromBill(billRequest, false);
	    
	    
	}
	
/*	public DemandResponse updateDemandFromBill(BillRequest billRequest, Boolean isReceiptCancellation) {
	    
		log.info("THE recieved bill request object------"+billRequest);
	    if(billRequest !=null && billRequest.getBills()!=null){

		List<Bill> bills = billRequest.getBills();
		RequestInfo requestInfo = billRequest.getRequestInfo();
		String tenantId = bills.get(0).getTenantId();
		Set<String> consumerCodes = new HashSet<>();
		for (Bill bill : bills) {
			for (BillDetail billDetail : bill.getBillDetails())
				consumerCodes.add(billDetail.getConsumerCode());
		}
		DemandCriteria demandCriteria = DemandCriteria.builder().consumerCode(consumerCodes).receiptRequired(false).tenantId(tenantId).build();
		List<Demand> demands = getDemands(demandCriteria, requestInfo).getDemands();
		log.info("THE DEMAND FETCHED FROM DB FOR THE GIVEN RECIEPT--------"+demands);
		Map<String, Demand> demandIdMap = demands.stream()
				.collect(Collectors.toMap(Demand::getId, Function.identity()));
		Map<String, List<Demand>> demandListMap = new HashMap<>();
		for (Demand demand : demands) {

			if (demandListMap.get(demand.getConsumerCode()) == null) {
				List<Demand> demands2 = new ArrayList<>();
				demands2.add(demand);
				demandListMap.put(demand.getConsumerCode(), demands2);
			} else
				demandListMap.get(demand.getConsumerCode()).add(demand);
		}

		for (Bill bill : bills) {
			for (BillDetail billDetail : bill.getBillDetails()) {

				List<Demand> demands2 = demandListMap.get(billDetail.getConsumerCode());
				Map<String, List<DemandDetail>> detailsMap = new HashMap<>();
				for (Demand demand : demands2) {
					for (DemandDetail demandDetail : demand.getDemandDetails()) {
						if (detailsMap.get(demandDetail.getTaxHeadMasterCode()) == null) {
							List<DemandDetail> demandDetails = new ArrayList<>();
							demandDetails.add(demandDetail);
							detailsMap.put(demandDetail.getTaxHeadMasterCode(), demandDetails);
						} else
							detailsMap.get(demandDetail.getTaxHeadMasterCode()).add(demandDetail);
					}
				}
					for (BillAccountDetail accountDetail : billDetail.getBillAccountDetails()) {

						if (accountDetail.getAccountDescription() != null && accountDetail.getAdjustedAmount() != null) {
							String[] array = accountDetail.getAccountDescription().split("-");
							log.info("the string array of values--------" + array.toString());

							List<String> accDescription = Arrays.asList(array);
							String taxHeadCode = accDescription.get(0);
							Long fromDate = Long.valueOf(accDescription.get(1));
							Long toDate = Long.valueOf(accDescription.get(2));

							for (DemandDetail demandDetail : detailsMap.get(taxHeadCode)) {
								log.info("the current demand detail : " + demandDetail);
								Demand demand = demandIdMap.get(demandDetail.getDemandId());
								log.info("the respective deman" + demand);

								if (fromDate.equals(demand.getTaxPeriodFrom())
										&& toDate.equals(demand.getTaxPeriodTo())) {

									BigDecimal collectedAmount = accountDetail.getCreditAmount();
									log.info("the credit amt :" + collectedAmount);
									//demandDetail.setTaxAmount(demandDetail.getTaxAmount().subtract(collectedAmount));
									
									 * If receipt cancellation is true, it will subtract the creditAmount from the collectionAmount
									 
									if(isReceiptCancellation) {
										demandDetail.setCollectionAmount(
												demandDetail.getCollectionAmount().subtract(collectedAmount));
									}else {
										demandDetail.setCollectionAmount(
												demandDetail.getCollectionAmount().add(collectedAmount));
									}
									log.info("the setTaxAmount ::: " + demandDetail.getTaxAmount());
									log.info("the setCollectionAmount ::: " + demandDetail.getCollectionAmount());
								}
							}
						}
					}
				}
		}
		
		demandRepository.update(new DemandRequest(requestInfo,demands));
	        DemandResponse demandResponse=new DemandResponse(responseInfoFactory.getResponseInfo(requestInfo, HttpStatus.OK),demands);
                kafkaTemplate.send(applicationProperties.getUpdateDemandBillTopicName(), demandResponse);
                
                kafkaTemplate.send(applicationProperties.getSaveCollectedReceipts(), billRequest);
                
		return demandResponse;
	    }
            return null;
	}
*/	
	
	
	
	/*
	 * 
	 * 
	 * @deprecated methods
	 * 
	 * 
	 * 
	 */
	
	@Deprecated
	public void saveCollectedReceipts(BillRequest billRequest) {
		List<BillDetail> billDetails = new ArrayList<>();
		for (Bill bill : billRequest.getBills()) {
			for (BillDetail detail : bill.getBillDetails())
				billDetails.add(detail);
		}
		demandRepository.saveCollectedReceipts(billDetails, billRequest.getRequestInfo());
	}
	
	/**
	 * Method to update only the collection amount in a demand
	 * 
	 * @param demandRequest
	 * @return
	 */
	@Deprecated
	public DemandResponse updateCollection(DemandRequest demandRequest) {

		log.debug("the demand service : " + demandRequest);
		RequestInfo requestInfo = demandRequest.getRequestInfo();
		List<Demand> demands = demandRequest.getDemands();
		AuditDetails auditDetail = getAuditDetail(requestInfo);

		Map<String, Demand> demandMap = demands.stream().collect(Collectors.toMap(Demand::getId, Function.identity()));
		Map<String, DemandDetail> demandDetailMap = new HashMap<>();
		for (Demand demand : demands) {
			for (DemandDetail demandDetail : demand.getDemandDetails())
				demandDetailMap.put(demandDetail.getId(), demandDetail);
		}
		DemandCriteria demandCriteria = DemandCriteria.builder().demandId(demandMap.keySet())
				.tenantId(demands.get(0).getTenantId()).build();
		List<Demand> existingDemands = demandRepository.getDemands(demandCriteria, null);
		 
		for (Demand demand : existingDemands) {

			AuditDetails demandAuditDetail = demand.getAuditDetails();
			demandAuditDetail.setLastModifiedBy(auditDetail.getLastModifiedBy());
			demandAuditDetail.setLastModifiedTime(auditDetail.getLastModifiedTime());

			for (DemandDetail demandDetail : demand.getDemandDetails()) {
				DemandDetail demandDetail2 = demandDetailMap.get(demandDetail.getId());
				BigDecimal tax = demandDetail.getTaxAmount().subtract(demandDetail2.getCollectionAmount());
				if(tax.doubleValue()>=0){
				//demandDetail.setTaxAmount(tax);
				demandDetail.setCollectionAmount(demandDetail.getCollectionAmount().add(demandDetail2.getCollectionAmount()));
				}
				
				AuditDetails demandDetailAudit = demandDetail.getAuditDetails();
				demandDetailAudit.setLastModifiedBy(auditDetail.getLastModifiedBy());
				demandDetailAudit.setLastModifiedTime(auditDetail.getLastModifiedTime());
			}
		}
		demandRequest.setDemands(existingDemands);
		kafkaTemplate.send(applicationProperties.getUpdateDemandTopic(), demandRequest);
		return new DemandResponse(responseInfoFactory.getResponseInfo(requestInfo, HttpStatus.CREATED),
				existingDemands);
	}
	
	@Deprecated
	/**
	 * Method to search only demand details
	 * 
	 * @param demandDetailCriteria
	 * @param requestInfo
	 * @return
	 */
	public DemandDetailResponse getDemandDetails(DemandDetailCriteria demandDetailCriteria, RequestInfo requestInfo) {

		return new DemandDetailResponse(responseInfoFactory.getResponseInfo(requestInfo, HttpStatus.OK),
				demandRepository.getDemandDetails(demandDetailCriteria));
	}
	
	@Deprecated
	//Demand update consumer code (update mis)
	public DemandResponse updateMISAsync(DemandUpdateMisRequest demandRequest) {

		kafkaTemplate.send(applicationProperties.getUpdateMISTopicName(), demandRequest);

		return new DemandResponse(responseInfoFactory.getResponseInfo(new RequestInfo(), HttpStatus.CREATED), null);
	}
	
	@Deprecated
	//update mis update method calling from kafka
	public void updateMIS(DemandUpdateMisRequest demandRequest){
		demandRepository.updateMIS(demandRequest);
	}
	
	@Deprecated
	public DemandDueResponse getDues(DemandDueCriteria demandDueCriteria, RequestInfo requestInfo) {
		
		Long currDate = new Date().getTime();
		Double currTaxAmt = 0d;
		Double currCollAmt = 0d;
		Double arrTaxAmt = 0d;
		Double arrCollAmt = 0d;

		DemandCriteria demandCriteria = DemandCriteria.builder().tenantId(demandDueCriteria.getTenantId())
				.businessService(demandDueCriteria.getBusinessService())
				.consumerCode(demandDueCriteria.getConsumerCode()).receiptRequired(false).build();
		
		List<Demand> demands = getDemands(demandCriteria, requestInfo).getDemands();
		for (Demand demand : demands) {
			if (demand.getTaxPeriodFrom() <= currDate && currDate <= demand.getTaxPeriodTo()) {
				for (DemandDetail detail : demand.getDemandDetails()) {
					currTaxAmt = currTaxAmt + detail.getTaxAmount().doubleValue();
					currCollAmt = currCollAmt + detail.getCollectionAmount().doubleValue();
				}
			} else if(currDate > demand.getTaxPeriodTo()){
				for (DemandDetail detail : demand.getDemandDetails()) {
					arrTaxAmt = arrTaxAmt + detail.getTaxAmount().doubleValue();
					arrCollAmt = arrCollAmt + detail.getCollectionAmount().doubleValue();
				}
			}
		}
		ConsolidatedTax consolidatedTax = ConsolidatedTax.builder().arrearsBalance(arrTaxAmt - arrCollAmt)
				.currentBalance(currTaxAmt - currCollAmt).arrearsDemand(arrTaxAmt).arrearsCollection(arrCollAmt)
				.currentDemand(currTaxAmt).currentCollection(currCollAmt).build();
		
		DemandDue due = DemandDue.builder().consolidatedTax(consolidatedTax).demands(demands).build();

		return new DemandDueResponse(responseInfoFactory.getResponseInfo(new RequestInfo(), HttpStatus.OK), due);
	}
	
	
}
