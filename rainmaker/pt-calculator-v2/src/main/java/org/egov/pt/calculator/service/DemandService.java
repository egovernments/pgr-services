package org.egov.pt.calculator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.calculator.repository.Repository;
import org.egov.pt.calculator.util.CalculatorConstants;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.web.models.Assessment;
import org.egov.pt.calculator.web.models.Calculation;
import org.egov.pt.calculator.web.models.CalculationCriteria;
import org.egov.pt.calculator.web.models.CalculationReq;
import org.egov.pt.calculator.web.models.GetBillCriteria;
import org.egov.pt.calculator.web.models.TaxHeadEstimate;
import org.egov.pt.calculator.web.models.demand.BillResponse;
import org.egov.pt.calculator.web.models.demand.Demand;
import org.egov.pt.calculator.web.models.demand.DemandDetail;
import org.egov.pt.calculator.web.models.demand.DemandRequest;
import org.egov.pt.calculator.web.models.demand.DemandResponse;
import org.egov.pt.calculator.web.models.demand.DemandStatus;
import org.egov.pt.calculator.web.models.demand.TaxHeadMaster;
import org.egov.pt.calculator.web.models.demand.TaxPeriod;
import org.egov.pt.calculator.web.models.property.OwnerInfo;
import org.egov.pt.calculator.web.models.property.Property;
import org.egov.pt.calculator.web.models.property.PropertyDetail;
import org.egov.pt.calculator.web.models.property.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class DemandService {

	@Autowired
	private EstimationService estimationService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Configurations configs;

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private CalculatorUtils utils;

	@Autowired
	private Repository repository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private PayService payService;

	@Autowired
	private MasterDataService mstrDataService;
	
	@Autowired
	private ReceiptService rcptService;

	/**
	 * Generates and persists the demand to billing service for the given property
	 * 
	 * if the property has been assessed already for the given financial year then
	 * 
	 * it carry forwards the old collection amount to the new demand as advance
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, Calculation> generateDemands(CalculationReq request) {

		List<CalculationCriteria> criterias = request.getCalculationCriteria();
		List<Demand> demands = new ArrayList<>();
		List<String> lesserAssessments = new ArrayList<>();
		Map<String, String> consumerCodeFinYearMap = new HashMap<>();
		
		Map<String, Calculation> propertyCalculationMap = estimationService.getEstimationPropertyMap(request);
		for (CalculationCriteria criteria : criterias) {

			PropertyDetail detail = criteria.getProperty().getPropertyDetails().get(0);
			
			String assessmentNumber = detail.getAssessmentNumber();

			// pt_tax for the new assessment
			BigDecimal newTax =  BigDecimal.ZERO;
			Optional<TaxHeadEstimate> advanceCarryforwardEstimate = propertyCalculationMap.get(assessmentNumber).getTaxHeadEstimates()
			.stream().filter(estimate -> estimate.getTaxHeadCode().equalsIgnoreCase(CalculatorConstants.PT_TAX))
				.findAny();
			if(advanceCarryforwardEstimate.isPresent())
				newTax = advanceCarryforwardEstimate.get().getEstimateAmount();

			// true represents that the demand should be updated from this call
			BigDecimal carryForwardCollectedAmount = getCarryForwardAndCancelOldDemand(newTax, criteria,
					request.getRequestInfo(), true);

			if (carryForwardCollectedAmount.doubleValue() >= 0.0) {
				Property property = criteria.getProperty();

				Demand demand = prepareDemand(property,
						propertyCalculationMap.get(property.getPropertyDetails().get(0).getAssessmentNumber()),
						request.getRequestInfo());

/*				if (carryForwardCollectedAmount.doubleValue() > 0.0)
					demand.getDemandDetails()
							.add(DemandDetail.builder().taxAmount(carryForwardCollectedAmount)
									.tenantId(criteria.getTenantId()).demandId(demand.getId())
									.taxHeadMasterCode(CalculatorConstants.PT_ADVANCE_CARRYFORWARD).build());*/
				
				/* ADVANCE_CARRYFORWARD case has already been handled while generating the estimate for this property, the above code is a redundant code that is 
				nullifying the effect of the same (commenting the code as asked by kavi.)*/ 
				
				demands.add(demand);
				consumerCodeFinYearMap.put(demand.getConsumerCode(), detail.getFinancialYear());
				
			}else {
				lesserAssessments.add(assessmentNumber);
			}
		}
		
		if (!CollectionUtils.isEmpty(lesserAssessments)) {
			throw new CustomException(CalculatorConstants.EG_PT_DEPRECIATING_ASSESSMENT_ERROR,
					CalculatorConstants.EG_PT_DEPRECIATING_ASSESSMENT_ERROR_MSG + lesserAssessments);
		}
		
		DemandRequest dmReq = DemandRequest.builder().demands(demands).requestInfo(request.getRequestInfo()).build();
		String url = new StringBuilder().append(configs.getBillingServiceHost())
				.append(configs.getDemandCreateEndPoint()).toString();
		DemandResponse res = new DemandResponse();

		try {
			res = restTemplate.postForObject(url, dmReq, DemandResponse.class);
		} catch (HttpClientErrorException e) {
			throw new ServiceCallException(e.getResponseBodyAsString());
		}
		log.info(" The demand Response is : " + res);
		assessmentService.saveAssessments(res.getDemands(), consumerCodeFinYearMap, request.getRequestInfo());
		return propertyCalculationMap;
	}

	/**
	 * Generates and returns bill from billing service
	 * 
	 * updates the demand with penalty and rebate if applicable before generating
	 * bill
	 * 
	 * @param getBillCriteria
	 * @param requestInfoWrapper
	 */
	public BillResponse getBill(GetBillCriteria getBillCriteria, RequestInfoWrapper requestInfoWrapper) {

		if(getBillCriteria.getAmountExpected() == null) getBillCriteria.setAmountExpected(BigDecimal.ZERO);
		RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();
		Map<String, Map<String, List<Object>>> propertyBasedExemptionMasterMap = new HashMap<>();
		Map<String, JSONArray> timeBasedExmeptionMasterMap = new HashMap<>();
		mstrDataService.setPropertyMasterValues(requestInfo, getBillCriteria.getTenantId(),
				propertyBasedExemptionMasterMap, timeBasedExmeptionMasterMap);

		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(getBillCriteria), requestInfoWrapper),
				DemandResponse.class);
		if (CollectionUtils.isEmpty(res.getDemands())) {
			Map<String, String> map = new HashMap<>();
			map.put(CalculatorConstants.EMPTY_DEMAND_ERROR_CODE, CalculatorConstants.EMPTY_DEMAND_ERROR_MESSAGE);
			throw new CustomException(map);
		}
		Demand demand = res.getDemands().get(0);
		
		if (demand.getStatus() != null
				&& CalculatorConstants.DEMAND_CANCELLED_STATUS.equalsIgnoreCase(demand.getStatus().toString()))
			throw new CustomException(CalculatorConstants.EG_PT_INVALID_DEMAND_ERROR,
					CalculatorConstants.EG_PT_INVALID_DEMAND_ERROR_MSG);

		applytimeBasedApplicables(demand, requestInfoWrapper, timeBasedExmeptionMasterMap);
		
/*		Map<String, Boolean> isTaxHeadDebitMap = mstrDataService
				.getTaxHeadMasterMap(requestInfoWrapper.getRequestInfo(), getBillCriteria.getTenantId()).stream()
				.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getIsDebit));

		BigDecimal totalTax = BigDecimal.ZERO;

		for (DemandDetail detail : demand.getDemandDetails()) {

			if (!isTaxHeadDebitMap.get(detail.getTaxHeadMasterCode()))
				totalTax = totalTax.add(detail.getTaxAmount());
			else
				totalTax = totalTax.subtract(detail.getTaxAmount());
		}

		if (BigDecimal.ZERO.compareTo(getBillCriteria.getAmountExpected()) < 0
				&& getBillCriteria.getAmountExpected().compareTo(totalTax) < 0)
			demand.getDemandDetails().forEach(detail -> {
				if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TIME_REBATE))
					detail.setTaxAmount(BigDecimal.ZERO);
			});
	*/	
		roundOffDecimalForDemand(demand, requestInfoWrapper);
		DemandRequest request = DemandRequest.builder().demands(Arrays.asList(demand)).requestInfo(requestInfo).build();
		StringBuilder updateDemandUrl = utils.getUpdateDemandUrl();
		repository.fetchResult(updateDemandUrl, request);
		StringBuilder billGenUrl = utils.getBillGenUrl(getBillCriteria.getTenantId(), demand.getId(), demand.getConsumerCode());
		return mapper.convertValue(repository.fetchResult(billGenUrl, requestInfoWrapper), BillResponse.class);
	}

	/**
	 * if any previous assessments and demands associated with it exists for the
	 * same financial year
	 * 
	 * Then Returns the collected amount of previous demand if the current
	 * assessment is for the current year
	 * 
	 * and cancels the previous demand by updating it's status to inactive
	 * 
	 * @param criteria
	 * @return
	 */
	protected BigDecimal getCarryForwardAndCancelOldDemand(BigDecimal newTax, CalculationCriteria criteria, RequestInfo requestInfo
			, boolean cancelDemand) {

		Property property = criteria.getProperty();

		BigDecimal carryForward = BigDecimal.ZERO;
		BigDecimal oldTaxAmt = BigDecimal.ZERO;

		if(null == property.getPropertyId()) return carryForward;

		Demand demand = getLatestDemandForCurrentFinancialYear(requestInfo, property);
		
		if(null == demand) return carryForward;

		carryForward = utils.getTotalCollectedAmountAndPreviousCarryForward(demand);
		
		for (DemandDetail detail : demand.getDemandDetails()) {
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TAX))
				oldTaxAmt = oldTaxAmt.add(detail.getTaxAmount());
		}			

		log.debug("The old tax amount in string : " + oldTaxAmt.toPlainString());
		log.debug("The new tax amount in string : " + newTax.toPlainString());
		
		if (oldTaxAmt.compareTo(newTax) > 0)
			carryForward = BigDecimal.valueOf(-1);

		if (BigDecimal.ZERO.compareTo(carryForward) >= 0 || !cancelDemand) return carryForward;
		
		demand.setStatus(DemandStatus.CANCELLED);
		DemandRequest request = DemandRequest.builder().demands(Arrays.asList(demand)).requestInfo(requestInfo).build();
		StringBuilder updateDemandUrl = utils.getUpdateDemandUrl();
		repository.fetchResult(updateDemandUrl, request);

		return carryForward;
	}

	/**
	 * @param requestInfo
	 * @param property
	 * @param carryForward
	 * @return
	 */
	public Demand getLatestDemandForCurrentFinancialYear(RequestInfo requestInfo, Property property) {
		
		Assessment assessment = Assessment.builder().propertyId(property.getPropertyId())
				.tenantId(property.getTenantId())
				.assessmentYear(property.getPropertyDetails().get(0).getFinancialYear()).build();

		List<Assessment> assessments = assessmentService.getMaxAssessment(assessment);

		if (CollectionUtils.isEmpty(assessments))
			return null;

		Assessment latestAssessment = assessments.get(0);
		log.debug(" the latest assessment : " + latestAssessment);

		DemandResponse res = mapper.convertValue(
				repository.fetchResult(utils.getDemandSearchUrl(latestAssessment), new RequestInfoWrapper(requestInfo)),
				DemandResponse.class);
		return res.getDemands().get(0);
	}

	/**
	 * Prepares Demand object based on the incoming calculation object and property
	 * 
	 * @param property
	 * @param calculation
	 * @return
	 */
	private Demand prepareDemand(Property property, Calculation calculation, RequestInfo requestInfo) {

		String tenantId = property.getTenantId();
		PropertyDetail detail = property.getPropertyDetails().get(0);
		String propertyType = detail.getPropertyType();
		String consumerCode = property.getPropertyId() + CalculatorConstants.PT_CONSUMER_CODE_SEPARATOR + detail.getAssessmentNumber();
		OwnerInfo owner = null;
		if (null != detail.getCitizenInfo())
			owner = detail.getCitizenInfo();
		else
			owner = detail.getOwners().iterator().next();
		
		Demand demand = getLatestDemandForCurrentFinancialYear(requestInfo, property);
		Map<String, BigDecimal> detailCollectionMap = new HashMap<>();
		if (null != demand)
			detailCollectionMap = demand.getDemandDetails().stream()
					.collect(Collectors.toMap(DemandDetail::getTaxHeadMasterCode, DemandDetail::getCollectionAmount));

		List<DemandDetail> details = new ArrayList<>();

		for (TaxHeadEstimate estimate : calculation.getTaxHeadEstimates()) {

			if (estimate.getTaxHeadCode().equalsIgnoreCase(CalculatorConstants.PT_ADVANCE_CARRYFORWARD))
				continue;

			details.add(DemandDetail.builder().taxHeadMasterCode(estimate.getTaxHeadCode())
					.taxAmount(estimate.getEstimateAmount())
					.collectionAmount(detailCollectionMap.get(estimate.getTaxHeadCode()) != null
							? detailCollectionMap.get(estimate.getTaxHeadCode())
							: BigDecimal.ZERO)
					.tenantId(tenantId).build());
		}

		return Demand.builder().tenantId(tenantId).businessService(configs.getPtModuleCode()).consumerType(propertyType)
				.consumerCode(consumerCode).owner(owner).taxPeriodFrom(calculation.getFromDate())
				.taxPeriodTo(calculation.getToDate()).status(DemandStatus.ACTIVE)
				.minimumAmountPayable(BigDecimal.valueOf(configs.getPtMinAmountPayable())).demandDetails(details)
				.build();
	}

	/**
	 * Applies Penalty/Rebate/Interest to the incoming demands
	 * 
	 * If applied already then the demand details will be updated
	 * 
	 * @param demand
	 * @param assessmentYear
	 * @return
	 */
	private boolean applytimeBasedApplicables(Demand demand,RequestInfoWrapper requestInfoWrapper,
			Map<String, JSONArray> timeBasedExmeptionMasterMap) {

		boolean isCurrentDemand = false;
		String tenantId = demand.getTenantId();
		String demandId = demand.getId();
		
		TaxPeriod taxPeriod = mstrDataService.getTaxPeriodList(requestInfoWrapper, tenantId).stream()
				.filter(t -> demand.getTaxPeriodFrom().compareTo(t.getFromDate()) >= 0
				&& demand.getTaxPeriodTo().compareTo(t.getToDate()) <= 0)
		.findAny().orElse(null);
		
		if(!(taxPeriod.getFromDate()<= System.currentTimeMillis() && taxPeriod.getToDate() >= System.currentTimeMillis()))
			isCurrentDemand = true;
		/*
		 * method to get the latest collected time from the receipt service
		 */
		Long lastCollectedTime = rcptService.getInterestLatestCollectedTime(taxPeriod.getFinancialYear(), demand,
				requestInfoWrapper);

		BigDecimal taxAmtForApplicableGeneration = utils.getTaxAmtFromDemandForApplicablesGeneration(demand);
		BigDecimal collectedApplicableAmount = BigDecimal.ZERO;
		BigDecimal totalCollectedAmount = BigDecimal.ZERO;
		BigDecimal oldInterest = BigDecimal.ZERO;

		for (DemandDetail detail : demand.getDemandDetails()) {
			
			totalCollectedAmount = totalCollectedAmount.add(detail.getCollectionAmount());
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TAX))
				collectedApplicableAmount = collectedApplicableAmount.add(detail.getCollectionAmount());
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TIME_INTEREST))
				oldInterest = detail.getTaxAmount();
		}
		
		boolean isRebateUpdated = false;
		boolean isPenaltyUpdated = false;
		boolean isInterestUpdated = false;
		
		List<DemandDetail> details = demand.getDemandDetails();
		
		Map<String, BigDecimal> rebatePenaltyEstimates = payService.applyPenaltyRebateAndInterest(taxAmtForApplicableGeneration,
				collectedApplicableAmount, lastCollectedTime, taxPeriod.getFinancialYear(), timeBasedExmeptionMasterMap);
		
		if(null == rebatePenaltyEstimates) return isCurrentDemand;
		
		BigDecimal rebate = rebatePenaltyEstimates.get(CalculatorConstants.PT_TIME_REBATE);
		BigDecimal penalty = rebatePenaltyEstimates.get(CalculatorConstants.PT_TIME_PENALTY);
		BigDecimal interest = rebatePenaltyEstimates.get(CalculatorConstants.PT_TIME_INTEREST);
		
		if (lastCollectedTime > 0)
			interest = oldInterest.add(rebatePenaltyEstimates.get(CalculatorConstants.PT_TIME_INTEREST));
		
		for (DemandDetail detail : details) {
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TIME_REBATE)) {
				if (BigDecimal.ZERO.compareTo(totalCollectedAmount) == 0)
					detail.setTaxAmount(rebate);
				isRebateUpdated = true;
			}
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TIME_PENALTY)) {
				detail.setTaxAmount(penalty);
				isPenaltyUpdated = true;
			}
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_TIME_INTEREST)) {
				detail.setTaxAmount(interest);
				isInterestUpdated = true;
			}
		}
		
		if (!isPenaltyUpdated && penalty.compareTo(BigDecimal.ZERO) > 0)
			details.add(DemandDetail.builder().taxAmount(penalty).taxHeadMasterCode(CalculatorConstants.PT_TIME_PENALTY)
					.demandId(demandId).tenantId(tenantId).build());
		if (!isRebateUpdated && rebate.compareTo(BigDecimal.ZERO) > 0)
			details.add(DemandDetail.builder().taxAmount(rebate)
					.taxHeadMasterCode(CalculatorConstants.PT_TIME_REBATE).demandId(demandId).tenantId(tenantId)
					.build());
		if (!isInterestUpdated && interest.compareTo(BigDecimal.ZERO) > 0)
			details.add(
					DemandDetail.builder().taxAmount(interest).taxHeadMasterCode(CalculatorConstants.PT_TIME_INTEREST)
							.demandId(demandId).tenantId(tenantId).build());
		
		return isCurrentDemand;
	}

	/**
	 * 
	 * Balances the decimal values in the newly updated demand by performing a roundoff
	 * 
	 * @param demand
	 * @param requestInfoWrapper
	 */
	public void roundOffDecimalForDemand(Demand demand, RequestInfoWrapper requestInfoWrapper) {
		
		List<DemandDetail> details = demand.getDemandDetails();
		String tenantId = demand.getTenantId();
		String demandId = demand.getId();
		
		/*
		 * isDecimalMatching 
		 * 
		 * This boolean variable will be set to true in case of the decimal tax-heads already being present in the demand
		 * 
		 * given that only debit or credit can exist at a time with value greater than zero
		 */
		boolean isDecimalMathcing = false;

		BigDecimal creditAmt = BigDecimal.ZERO;
		BigDecimal debitAmt = BigDecimal.ZERO;
		BigDecimal totalCollectedAmount = BigDecimal.ZERO;

		// Collecting the taxHead master codes with the isDebit field in a Map 
		Map<String, Boolean> isTaxHeadDebitMap = mstrDataService.getTaxHeadMasterMap(requestInfoWrapper.getRequestInfo(), tenantId).stream()
				.collect(Collectors.toMap(TaxHeadMaster::getCode, TaxHeadMaster::getIsDebit));

		/*
		 * Summing the credit amount and Debit amount in to separate variables(based on the taxhead:isdebit map) to send to roundoffDecimal method
		 */
		for (DemandDetail detail : demand.getDemandDetails()) {

			totalCollectedAmount = totalCollectedAmount.add(detail.getCollectionAmount());

			if (!isTaxHeadDebitMap.get(detail.getTaxHeadMasterCode())
					&& !detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_DECIMAL_CEILING_CREDIT))
				creditAmt = creditAmt.add(detail.getTaxAmount());
			else if (isTaxHeadDebitMap.get(detail.getTaxHeadMasterCode())
					&& !detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_DECIMAL_CEILING_DEBIT))
				debitAmt = debitAmt.add(detail.getTaxAmount());
		}

		/*
		 *  An estimate object will be returned incase if there is a decimal value
		 *  
		 *  If no decimal value found null object will be returned 
		 */
		TaxHeadEstimate estimate = payService.roundOfDecimals(creditAmt, debitAmt.add(totalCollectedAmount));

		/*
		 *  setting value to decimalCredit/decimalDebit fields in case estimate is available, in case of null Zero will be set
		 *  
		 *  if taxhead code is credit then debit will be zero and vice versa
		 */
		
		BigDecimal decimalCredit = null != estimate
				&& estimate.getTaxHeadCode().equalsIgnoreCase(CalculatorConstants.PT_DECIMAL_CEILING_CREDIT)
						? estimate.getEstimateAmount() : BigDecimal.ZERO;

		BigDecimal decimalDebit = null != estimate
				&& estimate.getTaxHeadCode().equalsIgnoreCase(CalculatorConstants.PT_DECIMAL_CEILING_DEBIT)
						? estimate.getEstimateAmount() : BigDecimal.ZERO;

		/*
		 *  Looping the demandDetails to update the tax Amount for the decimal demandDetails if they present
		 *  
		 *  If any decimal Detail found the isDecimalMatching will be set to true  
		 */
						
						
		for (DemandDetail detail : demand.getDemandDetails()) {

			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_DECIMAL_CEILING_CREDIT)) {
				if (detail.getCollectionAmount().compareTo(BigDecimal.ZERO) == 0)
					detail.setTaxAmount(decimalCredit);
				if (null != estimate
						&& CalculatorConstants.PT_DECIMAL_CEILING_CREDIT.equalsIgnoreCase(estimate.getTaxHeadCode()))
					isDecimalMathcing = true;
			}

			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(CalculatorConstants.PT_DECIMAL_CEILING_DEBIT)) {
				detail.setTaxAmount(decimalDebit);
				if (null != estimate
						&& CalculatorConstants.PT_DECIMAL_CEILING_DEBIT.equalsIgnoreCase(estimate.getTaxHeadCode()))
				isDecimalMathcing = true;
			}
		}

		/*
		 *  If isDecimalMatching set to false (meaning no decimal Detail is found already)
		 *  
		 *   then a new demandDetail will be created and added to the Demand. 
		 */
		if (!isDecimalMathcing && null != estimate && BigDecimal.ZERO.compareTo(estimate.getEstimateAmount()) <= 0)
			details.add(DemandDetail.builder().taxAmount(estimate.getEstimateAmount())
					.taxHeadMasterCode(estimate.getTaxHeadCode()).demandId(demandId).tenantId(tenantId).build());
	}
}
