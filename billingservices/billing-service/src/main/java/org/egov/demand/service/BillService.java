/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.demand.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.helper.BillHelper;
import org.egov.demand.model.Bill;
import org.egov.demand.model.BillAccountDetail;
import org.egov.demand.model.BillAccountDetail.PurposeEnum;
import org.egov.demand.model.BillDetail;
import org.egov.demand.model.BillSearchCriteria;
import org.egov.demand.model.BusinessServiceDetail;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.GenerateBillCriteria;
import org.egov.demand.model.GlCodeMaster;
import org.egov.demand.model.GlCodeMasterCriteria;
import org.egov.demand.model.TaxHeadMaster;
import org.egov.demand.model.TaxHeadMasterCriteria;
import org.egov.demand.model.enums.Category;
import org.egov.demand.repository.BillRepository;
import org.egov.demand.web.contract.BillRequest;
import org.egov.demand.web.contract.BillResponse;
import org.egov.demand.web.contract.BusinessServiceDetailCriteria;
import org.egov.demand.web.contract.DemandResponse;
import org.egov.demand.web.contract.User;
import org.egov.demand.web.contract.factory.ResponseFactory;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillService {

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private ResponseFactory responseFactory;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private BillRepository billRepository;

	@Autowired
	private BillHelper billHelper;

	@Autowired
	private DemandService demandService;

	@Autowired
	private BusinessServDetailService businessServDetailService;

	@Autowired
	private TaxHeadMasterService taxHeadMasterService;
	
	@Autowired
	private GlCodeMasterService glCodeMasterService;

	public BillResponse searchBill(BillSearchCriteria billCriteria, RequestInfo requestInfo){
		
		return getBillResponse(billRepository.findBill(billCriteria));
	}
	
	public BillResponse createAsync(BillRequest billRequest) {

		billHelper.getBillRequestWithIds(billRequest);

		try {
			kafkaTemplate.send(applicationProperties.getCreateBillTopic(),applicationProperties.getCreateBillTopicKey(),billRequest);
		} catch (Exception e) {
			log.debug("BillService createAsync:"+e);
			throw new RuntimeException(e);

		}
		return getBillResponse(billRequest.getBills());
	}

	public void create(BillRequest billRequest){
		billRepository.saveBill(billRequest);
	}

	public BillResponse generateBill(GenerateBillCriteria billCriteria, RequestInfo requestInfo) {

		Set<String> ids = new HashSet<>();
		if(billCriteria.getDemandId()!=null)
			ids.add(billCriteria.getDemandId());

		Set<String> consumerCodes = new HashSet<>();
		if (billCriteria.getConsumerCode() != null)
			consumerCodes.add(billCriteria.getConsumerCode());
		DemandCriteria demandCriteria = DemandCriteria.builder().businessService(billCriteria.getBusinessService())
				.consumerCode(consumerCodes).demandId(ids).email(billCriteria.getEmail()).receiptRequired(false)
				.mobileNumber(billCriteria.getMobileNumber()).tenantId(billCriteria.getTenantId()).build();

		log.debug("generateBill demandCriteria: "+demandCriteria);
		DemandResponse demandResponse = demandService.getDemands(demandCriteria,requestInfo);
		log.debug("generateBill demandResponse: "+demandResponse);
		List<Demand> demands = demandResponse.getDemands();

		List<Bill> bills = null;

		if(!demands.isEmpty())
			bills = prepareBill(demands, billCriteria.getTenantId(), requestInfo);
		else
			throw new RuntimeException("No Demands Found for the given criteria");

		if (bills.get(0).getBillDetails() == null) {
			return new BillResponse(responseFactory.getResponseInfo(requestInfo, HttpStatus.OK), null);
		} else
			return createAsync(BillRequest.builder().bills(bills).requestInfo(requestInfo).build());
	}

	private List<Bill> prepareBill(List<Demand> demands,String tenantId,RequestInfo requestInfo){

		List<Bill> bills = new ArrayList<>();

		Map<String, List<TaxHeadMaster>> taxHeadCodes = getTaxHeadMaster(demands, tenantId, requestInfo);

		log.debug("prepareBill taxHeadCodes:" + taxHeadCodes);

		Map<Pair<String, String>, List<Demand>> map = demands.stream()
				.collect(Collectors.groupingBy(t -> Pair.of(t.getBusinessService(), t.getConsumerCode())));
		
		Set<Pair<String, String>> businessConsumerPairMap = map.keySet();
		//Set<Pair<String, String>> businessConsumerMap = map.keySet();
		Set<String> businessServices = businessConsumerPairMap.stream().map(Pair::getLeft).collect(Collectors.toSet());
		Map<String,BusinessServiceDetail> businessServiceMap = getBusinessService(businessServices,tenantId,requestInfo);
		log.info("prepareBill map:" +map);
		Demand demand = demands.get(0);
		User owner = demand.getPayer();
		// please add address to user object TODO FIXME
		Bill bill = Bill.builder().isActive(true).isCancelled(false).payerAddress(null).
				payerEmail(owner.getEmailId()).payerName(owner.getName()).mobileNumber(owner.getMobileNumber())
				.tenantId(tenantId).build();

		List<BillDetail> billDetails = new ArrayList<>();

		for(Map.Entry<Pair<String, String>, List<Demand>> entry : map.entrySet()){
			Pair<String, String> businessConsumerPair = entry.getKey();
			String businessService = businessConsumerPair.getLeft();
			List<Demand> demands2 = entry.getValue();
			log.info("prepareBill demands2:" +demands2);
			
			List<BillAccountDetail> billAccountDetails = new ArrayList<>();
			
			Map<String, List<GlCodeMaster>> glCodesMap = getGlCodes(demands2, businessService, tenantId, requestInfo);
			log.info("prepareBill glCodesMap:" +glCodesMap);
			
			Demand demand3 = demands2.get(0);
			BigDecimal totalTaxAmount = BigDecimal.ZERO;
			BigDecimal totalMinAmount = BigDecimal.ZERO;
			BigDecimal totalCollectedAmount = BigDecimal.ZERO;
			BigDecimal totalDebitAmount = BigDecimal.ZERO;

			for(Demand demand2 : demands2){
				List<DemandDetail> demandDetails = demand2.getDemandDetails();
				log.info("prepareBill demandDetails:" +demandDetails);
				log.info("prepareBill demand2:" +demand2);

				totalMinAmount = totalMinAmount.add(demand2.getMinimumAmountPayable());
				for (DemandDetail demandDetail : demandDetails) {

					log.debug("prepareBill demandDetail:" + demandDetail);
					String taxHeadCode = demandDetail.getTaxHeadMasterCode();

					List<TaxHeadMaster> taxHeadMasters = taxHeadCodes.get(taxHeadCode);
					TaxHeadMaster taxHeadMaster = taxHeadMasters.stream().filter(t -> 
					demand2.getTaxPeriodFrom().compareTo(t.getValidFrom()) >= 0 && demand2.getTaxPeriodTo().
					compareTo(t.getValidTill()) <= 0).findAny().orElse(null);
					
					if(taxHeadMaster == null) 
						throw new RuntimeException(
								"No TaxHead Found for demandDetail with taxcode :"+demandDetail.getTaxHeadMasterCode()
								+"  and fromdate : --"+demand2.getTaxPeriodFrom() + "  and todate-----"+demand2.getTaxPeriodTo());

					List<GlCodeMaster> glCodeMasters = glCodesMap.get(demandDetail.getTaxHeadMasterCode());

					if(isEmpty(glCodeMasters))
						throw new RuntimeException("no glcodemasters found for the given taxhead master code"+demandDetail.getTaxHeadMasterCode());
					log.info("prepareBill glCodeMasters:" + glCodeMasters);
					GlCodeMaster glCodeMaster = glCodeMasters.stream()
							.filter(t -> demand2.getTaxPeriodFrom() >= t.getFromDate()
									&& demand2.getTaxPeriodTo() <= t.getToDate())
							.findAny().orElse(null);
					
					if(glCodeMaster == null) 
						throw new RuntimeException(
								"No glCode Found for taxcode : "+demandDetail.getTaxHeadMasterCode() 
								+ " and fromdate : "+demand2.getTaxPeriodFrom()+" todate : "+demand2.getTaxPeriodTo());

					log.info("prepareBill taxHeadMaster:" + taxHeadMaster);
					
					PurposeEnum purpose = getPurpose(
							taxHeadMaster.getCategory(),taxHeadCode,demand2.getTaxPeriodFrom(),demand2.getTaxPeriodTo());
					
					String accountDespcription = taxHeadCode+"-"+demand2.getTaxPeriodFrom()+"-"+demand2.getTaxPeriodTo();
					
					// add tax head code FIXME TODO account description removed
					BillAccountDetail billAccountDetail = BillAccountDetail.builder()
							.glcode(glCodeMaster.getGlCode()).isActualDemand(taxHeadMaster.getIsActualDemand())
							.order(taxHeadMaster.getOrder()).purpose(purpose)
							.build();

					/*
					 * collecting values of total tax amount , collection amount and debit amount
					 */
					// Review the bill gen logic FIXME TODO
					totalCollectedAmount = totalCollectedAmount.add(demandDetail.getCollectionAmount());
					if (taxHeadMaster.getIsDebit()) {
						billAccountDetail.setAmount(demandDetail.getTaxAmount());
						totalDebitAmount = totalDebitAmount.add(demandDetail.getTaxAmount());
					}
					else {
						billAccountDetail.setAmount(demandDetail.getTaxAmount().subtract(
								demandDetail.getCollectionAmount() != null ? demandDetail.getCollectionAmount()
										: BigDecimal.ZERO));
						totalTaxAmount = totalTaxAmount.add(demandDetail.getTaxAmount());
					}
						
					billAccountDetails.add(billAccountDetail);
				}
			}

			BusinessServiceDetail businessServiceDetail = businessServiceMap.get(businessService);
			String description = demand3.getBusinessService() + " Consumer Code: " + demand3.getConsumerCode();

			BillDetail billDetail = BillDetail.builder().businessService(demand3.getBusinessService())
					// description removed please add tax head code to the billAccountDetail
					/*.billDescription(description)*/.billAccountDetails(billAccountDetails)
					.billDate(new Date().getTime())
					
					.collectionModesNotAllowed(businessServiceDetail.getCollectionModesNotAllowed())
					.consumerType(demand3.getConsumerType()).consumerCode(demand3.getConsumerCode())
					.minimumAmount(totalMinAmount).partPaymentAllowed(businessServiceDetail.getPartPaymentAllowed())
					.totalAmount(totalTaxAmount.subtract(totalDebitAmount).subtract(totalCollectedAmount))
					.collectedAmount(totalCollectedAmount).tenantId(tenantId).build();

			/*
			 * bill with zero gen enabled, remove the commented if condition to block bills
			 * with zero amount to be collected
			 */
			// if(billDetail.getTotalAmount().compareTo(BigDecimal.ZERO) > 0)
			billDetails.add(billDetail);

		}
		if (!billDetails.isEmpty())
			bill.setBillDetails(billDetails);
		bills.add(bill);

		return bills;
	}

	/**
	 * Applies the purpose on Bill Account Details
	 * @param category
	 * @param taxHeadCode
	 * @param taxPeriodFrom
	 * @param taxPeriodTo
	 * @return
	 */
	private PurposeEnum getPurpose(Category category, String taxHeadCode, Long taxPeriodFrom, Long taxPeriodTo) {

		Long currDate = System.currentTimeMillis();

		if (category.equals(Category.TAX) || category.equals(Category.FEE) || category.equals(Category.CHARGES)) {

			if (taxPeriodFrom <= currDate && taxPeriodTo >= currDate)
				return PurposeEnum.CURRENT;
			else if (currDate > taxPeriodTo)
				return PurposeEnum.ARREAR;
			else
				return PurposeEnum.ADVANCE;
		}else if (category.equals(Category.ADVANCE_COLLECTION)) {
			return PurposeEnum.ADVANCE;
		} else {
			return PurposeEnum.OTHERS;
		}
	}

	private Map<String, List<TaxHeadMaster>> getTaxHeadMaster(List<Demand> demands, String tenantId, RequestInfo requestInfo) {

		List<DemandDetail> demandDetails = new ArrayList<>();
		for(Demand demand : demands){
			demandDetails.addAll(demand.getDemandDetails());
		}

		log.debug("getTaxHeadMaster demandDetails:"+demandDetails);

		Set<String>  taxHeadMasterCode = demandDetails.stream().
				map(demandDetail -> demandDetail.getTaxHeadMasterCode()).collect(Collectors.toSet());

		log.debug("getTaxHeadMaster taxHeadMasterCode:"+taxHeadMasterCode);
		List<TaxHeadMaster> taxHeadMasters = taxHeadMasterService.getTaxHeads(
				TaxHeadMasterCriteria.builder().tenantId(tenantId).code(taxHeadMasterCode).build(),requestInfo).getTaxHeadMasters();
		if(taxHeadMasters.isEmpty())
			throw new RuntimeException("No TaxHead masters found for the given criteria");
		log.debug("getTaxHeadMaster taxHeadMasters:"+taxHeadMasters);
		Map<String, List<TaxHeadMaster>> map = taxHeadMasters.stream().collect(Collectors.groupingBy(TaxHeadMaster::getCode, Collectors.toList()));

		log.debug("getTaxHeadMaster map:"+map);
		return map;
	}

	private Map<String, List<GlCodeMaster>> getGlCodes(List<Demand> demands, String service,String tenantId, RequestInfo requestInfo) {

		List<DemandDetail> demandDetails = new ArrayList<>();
		for(Demand demand : demands){
			demandDetails.addAll(demand.getDemandDetails());
		}

		log.debug("getGlCodes demandDetails:"+demandDetails);

		Set<String>  taxHeadMasterCode = demandDetails.stream().
				map(demandDetail -> demandDetail.getTaxHeadMasterCode()).collect(Collectors.toSet());

		log.debug("getGlCodes taxHeadMasterCode:"+taxHeadMasterCode);
		List<GlCodeMaster> glCodeMasters = glCodeMasterService.getGlCodes(
				GlCodeMasterCriteria.builder().taxHead(taxHeadMasterCode).service(
				service).tenantId(tenantId).build(), requestInfo).getGlCodeMasters();
		log.debug("getGlCodes glCodeMasters:"+glCodeMasters);
		if(glCodeMasters.isEmpty())
			throw new RuntimeException("No GlcodeMasters found for the given criteria");
		Map<String, List<GlCodeMaster>> map = glCodeMasters.stream().collect(
				Collectors.groupingBy(GlCodeMaster::getTaxHead, Collectors.toList()));

		log.debug("getTaxHeadMaster map:"+map);
		return map;
	}
	
	private Map<String, BusinessServiceDetail> getBusinessService(Set<String> businessService, String tenantId, RequestInfo requestInfo) {
		List<BusinessServiceDetail> businessServiceDetails = businessServDetailService.searchBusinessServiceDetails(BusinessServiceDetailCriteria.builder().businessService(businessService).tenantId(tenantId).build(), requestInfo)
				.getBusinessServiceDetails();
		return businessServiceDetails.stream().collect(Collectors.toMap(BusinessServiceDetail::getBusinessService, Function.identity()));
	}
	
	public BillResponse getBillResponse(List<Bill> bills) {
		BillResponse billResponse = new BillResponse();
		billResponse.setBill(bills);
		return billResponse;
	}

	public BillResponse apportion(BillRequest billRequest) {
		return new BillResponse(responseFactory.getResponseInfo(billRequest.getRequestInfo(), HttpStatus.OK), billRepository.apportion(billRequest));
	}
}
