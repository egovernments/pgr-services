package org.egov.demand.web.validator;

import static org.egov.demand.util.Constants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.model.Demand;
import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetail;
import org.egov.demand.model.TaxPeriod;
import org.egov.demand.repository.DemandRepository;
import org.egov.demand.repository.ServiceRequestRepository;
import org.egov.demand.util.Util;
import org.egov.demand.web.contract.DemandRequest;
import org.egov.demand.web.contract.User;
import org.egov.demand.web.contract.UserResponse;
import org.egov.demand.web.contract.UserSearchRequest;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DemandValidatorV1 {

	@Autowired
	private ApplicationProperties properties;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private Util util;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private DemandRepository demandRepository;
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
	/**
	 * Method to validate new demand request
	 * 
	 * @param demandRequest 
	 */
	public void validatedemandForCreate(DemandRequest demandRequest, Boolean isCreate) {

		RequestInfo requestInfo = demandRequest.getRequestInfo();
		List<Demand> demands = demandRequest.getDemands();
		String tenantId = demands.get(0).getTenantId();

		/*
		 * Preparing the mdms request with billing service master and calling the mdms search API
		 */
		MdmsCriteriaReq mdmsReq = util.prepareMdMsRequest(tenantId, MODULE_NAME, MDMS_MASTER_NAMES, MDMS_CODE_FILTER,
				requestInfo);
		DocumentContext mdmsData = getAttributeValues(mdmsReq);

		/*
		 * Extracting the respective masters from DocumentContext 
		 */
		List<String> businessServiceCodes = mdmsData.read(BUSINESSSERVICE_PATH_CODE);
		List<String> taxHeadCodes = mdmsData.read(TAXHEADMASTER_PATH_CODE);
		
		/*
		 * mdmsdata read returns a list of hashmap which is converted to array of taxperiod and then to list
		 */
		List<TaxPeriod> taxPeriods = Arrays.asList(mapper.convertValue(mdmsData.read(TAXPERIOD_PATH_CODE), TaxPeriod[].class));

		/*
		 * Grouping by the list periods on business services
		 */
		Map<String, List<TaxPeriod>> taxPeriodBusinessMap = taxPeriods.stream()
				.collect(Collectors.groupingBy(TaxPeriod::getService));
		Set<String> payerIds = new HashSet<>();
		
		/* demand details list for validation */
		List<DemandDetail> detailsForValidation = new ArrayList<>();
		
		/* business consumer map for validating uniqueness of consumer code */
		Map<String, Set<String>> businessConsumerValidatorMap = new HashMap<>();
		
		Set<String> businessServicesWithNoTaxPeriods = new HashSet<>();
		Set<String> businessServicesNotFound = new HashSet<>();
		Set<String> taxHeadsNotFound = new HashSet<>();

		Map<String, String> errorMap = new HashMap<>();

		for (Demand demand : demands) {

			List<DemandDetail> details = demand.getDemandDetails();
			if (isCreate)
				detailsForValidation.addAll(details);
			
			if(null != demand.getPayer())
				payerIds.add(demand.getPayer().getUuid());
			
			if (!businessServiceCodes.contains(demand.getBusinessService()))
				businessServicesNotFound.add(demand.getBusinessService());

			
			details.forEach(detail -> {
				if (!taxHeadCodes.contains(detail.getTaxHeadMasterCode()))
					taxHeadsNotFound.add(detail.getTaxHeadMasterCode());
			});

			validateTaxPeriod(taxPeriodBusinessMap, demand, errorMap, businessServicesWithNoTaxPeriods);
			
			/* passing the businessConsumerValidator map to be enriched for validation */
			enrichConsumerCodesWithBusinessMap(businessConsumerValidatorMap, demand);
		}
		
		/*
		 * Validating payer(Citizen) data
		 */
		validatePayer(payerIds, requestInfo, errorMap);
		/*
		 * Validating demand details for tax and collection amount
		 * 
		 * if called from update no need to call detail validation 
		 */
		if (isCreate)
			validateDemandDetails(detailsForValidation, errorMap);
		
		validateConsumerCodes(demands, businessConsumerValidatorMap, errorMap);

		if (!CollectionUtils.isEmpty(taxHeadsNotFound))
			errorMap.put(TAXHEADS_NOT_FOUND_KEY,
					TAXHEADS_NOT_FOUND_MSG.replace(TAXHEADS_NOT_FOUND_REPLACETEXT, taxHeadsNotFound.toString()));

		if (!CollectionUtils.isEmpty(businessServicesNotFound))
			errorMap.put(BUSINESSSERVICE_NOT_FOUND_KEY, BUSINESSSERVICE_NOT_FOUND_MSG
					.replace(BUSINESSSERVICE_NOT_FOUND_REPLACETEXT, businessServicesNotFound.toString()));

		if (!CollectionUtils.isEmpty(businessServicesWithNoTaxPeriods))
			errorMap.put(INVALID_BUSINESS_FOR_TAXPERIOD_KEY, INVALID_BUSINESS_FOR_TAXPERIOD_MSG
					.replace(INVALID_BUSINESS_FOR_TAXPERIOD_REPLACE_TEXT, businessServicesWithNoTaxPeriods.toString()));

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}

	/**
	 * Method to validate the tax period of the demand
	 * 
	 * @param taxPeriodBusinessMap
	 * @param demand
	 * @param errorMap
	 * @param businessServicesWithNoTaxPeriods
	 */
	private void validateTaxPeriod(Map<String, List<TaxPeriod>> taxPeriodBusinessMap, Demand demand,
			Map<String, String> errorMap, Set<String> businessServicesWithNoTaxPeriods) {
		
		/*
		 * Getting the list of tax periods belonging to the current business service
		 */
		List<TaxPeriod> taxPeriods = taxPeriodBusinessMap.get(demand.getBusinessService());


		if (taxPeriods != null) {

			/*
			 * looping the list of business services to check if the given demand periods gets match
			 */
			TaxPeriod taxPeriod = taxPeriods.stream()
					.filter(t -> demand.getTaxPeriodFrom().compareTo(t.getFromDate()) >= 0
							&& demand.getTaxPeriodTo().compareTo(t.getToDate()) <= 0)
					.findAny().orElse(null);

			if (taxPeriod == null) {

				String msg = TAXPERIOD_NOT_FOUND_MSG
						.replace(TAXPERIOD_NOT_FOUND_FROMDATE, demand.getTaxPeriodFrom().toString())
						.replace(TAXPERIOD_NOT_FOUND_TODATE, demand.getTaxPeriodTo().toString());
				errorMap.put(TAXPERIOD_NOT_FOUND_KEY, msg);
			}

		} else {
			/*
			 * Adding business service name to the set "businessServicesWithNoTaxPeriods"
			 */
			businessServicesWithNoTaxPeriods.add(demand.getBusinessService());
		}

	}

	/**
	 * Method to enrich the businessConsumerMap which will be passed to consumer code validator
	 * 
	 * @param businessConsumerValidatorMap
	 * @param demand
	 */
	private void enrichConsumerCodesWithBusinessMap(Map<String, Set<String>> businessConsumerValidatorMap,
			Demand demand) {

		Set<String> consumerCodes = businessConsumerValidatorMap.get(demand.getBusinessService());
		if (consumerCodes != null)
			consumerCodes.add(demand.getConsumerCode());
		else {
			consumerCodes = new HashSet<>();
			consumerCodes.add(demand.getConsumerCode());
			businessConsumerValidatorMap.put(demand.getBusinessService(), consumerCodes);
		}
	}
	
	/**
	 * Method to validate the Consumer codes in demand request for period and business Code
	 * 
	 * @param demands list of demand to be validated
	 * @param errorMap map with error key and msg
	 */
	private void validateConsumerCodes(List<Demand> demands, Map<String, Set<String>> businessConsumerValidatorMap,
			Map<String, String> errorMap) {

		String tenantId = demands.get(0).getTenantId();
		List<String> errors = new ArrayList<>();

		List<Demand> dbDemands = demandRepository.getDemandsForConsumerCodes(businessConsumerValidatorMap, tenantId);
		Map<String, List<Demand>> dbDemandMap = dbDemands.stream()
				.collect(Collectors.groupingBy(Demand::getConsumerCode, Collectors.toList()));

		if (!dbDemandMap.isEmpty()) {
			for (Demand demand : demands) {
				for (Demand demandFromMap : dbDemandMap.get(demand.getConsumerCode())) {
					if (demand.getTaxPeriodFrom().equals(demandFromMap.getTaxPeriodFrom())
							&& demand.getTaxPeriodTo().equals(demandFromMap.getTaxPeriodTo()))
						errors.add(demand.getConsumerCode());
				}
			}
		}
		
		if(!CollectionUtils.isEmpty(errors))
			errorMap.put(CONSUMER_CODE_DUPLICATE_KEY,
					CONSUMER_CODE_DUPLICATE_MSG.replace(CONSUMER_CODE_DUPLICATE_CONSUMERCODE_TEXT, errors.toString()));
	}
	
    /**
     * Fetches all the values of particular attribute as documentContext
     *
     * @param tenantId tenantId of properties in PropertyRequest
     * @param names List of String containing the names of all masterdata whose code has to be extracted
     * @param requestInfo RequestInfo of the received PropertyRequest
     * @return Map of MasterData name to the list of code in the MasterData
     *
     */
    private DocumentContext getAttributeValues(MdmsCriteriaReq mdmsReq){
        StringBuilder uri = new StringBuilder(properties.getMdmsHost()).append(properties.getMdmsEndpoint());
        
        try {
            return serviceRequestRepository.fetchResult(uri.toString(), mdmsReq);
        } catch (Exception e) {
            log.error("Error while fetvhing MDMS data",e);
            throw new CustomException(INVALID_TENANT_ID_MDMS_KEY, INVALID_TENANT_ID_MDMS_MSG);
        }
    }
    
    /**
     * Method to validate payer(user/citizen) data in demand
     * 
     * @param payerIds
     * @param requestInfo
     * @param errorMap
     */
	private void validatePayer(Set<String> payerIds, RequestInfo requestInfo, Map<String, String> errorMap) {

		String url = applicationProperties.getUserServiceHostName()
				.concat(applicationProperties.getUserServiceSearchPath());

		List<User> owners = null;
		Set<String> missingIds = new HashSet<>();

		UserSearchRequest userSearchRequest = UserSearchRequest.builder().requestInfo(requestInfo).uuid(payerIds)
				.pageSize(500).build();

		owners = mapper.convertValue(serviceRequestRepository.fetchResult(url, userSearchRequest), UserResponse.class).getUser();

		if (!CollectionUtils.isEmpty(owners)) {

			Map<String, String> ownerMap = owners.stream().collect(Collectors.toMap(User::getUuid, User::getUuid));

			/*
			 * Adding the missing ids to the list to be added to error map
			 */
			for (String uuid : payerIds)
				if (ownerMap.get(uuid) == null)
					missingIds.add(uuid);
			
			if(!CollectionUtils.isEmpty(missingIds))
				errorMap.put(USER_UUID_NOT_FOUND_KEY,
						USER_UUID_NOT_FOUND_MSG.replace(USER_UUID_NOT_FOUND_REPLACETEXT, missingIds.toString()));
		}
	}
	
	/**
	 * Method to validate demand details based on tax and collection amount
	 * 
	 * @param demandDetails
	 * @param errorMap
	 */
	private void validateDemandDetails(List<DemandDetail> demandDetails, Map<String, String> errorMap) {

		List<String> errors = new ArrayList<>();

		for (DemandDetail demandDetail : demandDetails) {

			BigDecimal tax = demandDetail.getTaxAmount();
			BigDecimal collection = demandDetail.getCollectionAmount();
			if (tax.compareTo(BigDecimal.ZERO) > 0 && tax.compareTo(collection) < 0) {
				errors.add(INVALID_DEMAND_DETAIL_ERROR_MSG
						.replace(INVALID_DEMAND_DETAIL_COLLECTION_TEXT, collection.toString())
						.replace(INVALID_DEMAND_DETAIL_TAX_TEXT, tax.toString()));
			} else if (tax.doubleValue() < 0 && (collection.doubleValue() != 0 && collection.compareTo(tax) != 0)) {

				errors.add(INVALID_NEGATIVE_DEMAND_DETAIL_ERROR_MSG
						.replace(INVALID_DEMAND_DETAIL_COLLECTION_TEXT, collection.toString())
						.replace(INVALID_DEMAND_DETAIL_TAX_TEXT, tax.toString()));
			}
		}
		if (!CollectionUtils.isEmpty(errors))
			errorMap.put(INVALID_DEMAND_DETAIL_KEY,
					INVALID_DEMAND_DETAIL_MSG.replace(INVALID_DEMAND_DETAIL_REPLACETEXT, errors.toString()));
	}
	

/*
 * 
 * update validation 
 * 
 * 
 */

	/**
	 * Method to validate the update request
	 * 
	 * internally calls the create method to validate the new demands
	 * @param demandRequest
	 * @param errorMap
	 */
	public void validateForUpdate(DemandRequest demandRequest) {

		Map<String, String> errorMap = new HashMap<>();
		List<Demand> demands = demandRequest.getDemands();
		String tenantId = demands.get(0).getTenantId();

		List<Demand> oldDemands = new ArrayList<>();
		List<DemandDetail> olddemandDetails = new ArrayList<>();
		List<Demand> newDemands = new ArrayList<>();
		List<DemandDetail> newDemandDetails = new ArrayList<>();

		for (Demand demand : demandRequest.getDemands()) {
			if (demand.getId() != null) {
				oldDemands.add(demand);
				for (DemandDetail demandDetail : demand.getDemandDetails()) {
					if (demandDetail.getId() != null)
						olddemandDetails.add(demandDetail);
					else
						newDemandDetails.add(demandDetail);
				}
			} else {
				newDemands.add(demand);
				newDemandDetails.addAll(demand.getDemandDetails());
			}
		}
		validateOldDemands(oldDemands, olddemandDetails, errorMap, tenantId);
		
		/*
		 * Validating all the demand details for tax and collection amount
		 */
		olddemandDetails.addAll(newDemandDetails);
		validateDemandDetails(olddemandDetails, errorMap);

		/*
		 * validate demand for Create is called to validate the new demands which is part of update
		 * 
		 * error map will be thrown in the create method itself
		 */
		validatedemandForCreate(demandRequest, false);
	}
	
	/**
	 * Method to validate the old demands for update
	 * 
	 * Queries the DB and validates the result with the request demands
	 * 
	 * @param oldDemands
	 * @param olddemandDetails
	 * @param errorMap
	 * @param tenantId
	 */
	private void validateOldDemands(List<Demand> oldDemands, List<DemandDetail> olddemandDetails, Map<String, String> errorMap,
			String tenantId) {

		List<String> unFoundDemandIds = new ArrayList<>();
		List<String> unFoundDemandDetailIds = new ArrayList<>();

		/*
		 * Demand search criteria creation
		 * 
		 * fetching data from db based on the demand ids of the request
		 * 
		 */
		Set<String> demandIds = oldDemands.stream().map(Demand::getId).collect(Collectors.toSet());
		DemandCriteria demandCriteria = DemandCriteria.builder().tenantId(tenantId).demandId(demandIds).build();
		Map<String, Demand> demandMap = demandRepository.getDemands(demandCriteria, null).stream()
				.collect(Collectors.toMap(Demand::getId, Function.identity()));
		Map<String, DemandDetail> dbDemandDetailMap = new HashMap<>();

		/*
		 * Collecting ids of the demand and demand detail for which no match has been found
		 */
		for (Demand demand : oldDemands) {
			Demand dbDemand = demandMap.get(demand.getId());
			if (dbDemand == null)
				unFoundDemandIds.add(demand.getId());
			else {
				dbDemandDetailMap.putAll(dbDemand.getDemandDetails().stream()
						.collect(Collectors.toMap(DemandDetail::getId, Function.identity())));
			}
		}

		for (DemandDetail demandDetail : olddemandDetails) {
			if (dbDemandDetailMap.get(demandDetail.getId()) == null)
				unFoundDemandDetailIds.add(demandDetail.getId());
		}
		
		if (!CollectionUtils.isEmpty(unFoundDemandIds))
			errorMap.put(DEMAND_NOT_FOUND_KEY,
					DEMAND_NOT_FOUND_MSG.replace(DEMAND_NOT_FOUND_REPLACETEXT, unFoundDemandIds.toString()));

		if (!CollectionUtils.isEmpty(unFoundDemandDetailIds))
			errorMap.put(DEMAND_DETAIL_NOT_FOUND_KEY, DEMAND_DETAIL_NOT_FOUND_MSG
					.replace(DEMAND_DETAIL_NOT_FOUND_REPLACETEXT, unFoundDemandDetailIds.toString()));
	}
	
	/*
	 * Search validation
	 */

	/**
	 * Method to validate demand search request
	 * 
	 * @param demandCriteria
	 */
	public void validateDemandCriteria(DemandCriteria demandCriteria) {

		Map<String, String> errorMap = new HashMap<>();

		if (demandCriteria.getDemandId() == null && demandCriteria.getConsumerCode() == null
				&& demandCriteria.getEmail() == null && demandCriteria.getMobileNumber() == null
				&& demandCriteria.getBusinessService() == null && demandCriteria.getDemandFrom() == null
				&& demandCriteria.getDemandTo() == null && demandCriteria.getType() == null)
			errorMap.put("businessService", " Any one of the fields additional to tenantId is mandatory");

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
}
