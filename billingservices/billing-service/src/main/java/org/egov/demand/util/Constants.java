package org.egov.demand.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Constants {
	
	private Constants() {}

	public static final String MODULE_NAME = "BillingService";
	
	public static final String MDMS_CODE_FILTER = "$.*.code";
	public static final String TAXPERIOD_CODE_FILTER = null;
	
	public static final String TAXPERIOD_PATH_CODE = "$.MdmsRes.BillingService.TaxPeriod";
	public static final String TAXHEADMASTER_PATH_CODE = "$.MdmsRes.BillingService.TaxHeadMaster";
	public static final String BUSINESSSERVICE_PATH_CODE = "$.MdmsRes.BillingService.BusinessService";
	
	public static final String TAXPERIOD_MASTERNAME = "TaxPeriod";
	public static final String TAXHEAD_MASTERNAME = "TaxHeadMaster";
	public static final String BUSINESSSERVICE_MASTERNAME = "BusinessService";
	
	public static final List<String> MDMS_MASTER_NAMES = Collections
			.unmodifiableList(Arrays.asList(TAXHEAD_MASTERNAME, TAXPERIOD_MASTERNAME, BUSINESSSERVICE_MASTERNAME));
	
    public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";
    public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";
    
    
    // ERROR CONSTANTS
    
    
	public static final String INVALID_BUSINESS_FOR_TAXPERIOD_KEY = "EG_BS_TAXPERIODS_BUINESSSERVICE";
	public static final String INVALID_BUSINESS_FOR_TAXPERIOD_MSG = "No Tax Periods Found for the given BusinessServices value of {resplaceValues}";
	public static final String INVALID_BUSINESS_FOR_TAXPERIOD_REPLACE_TEXT = "{resplaceValues}";

	public static final String TAXPERIOD_NOT_FOUND_KEY = "EG_BS_TAXPERIODS_DEMAND";
	public static final String TAXPERIOD_NOT_FOUND_MSG = "No Tax Periods Found for the given demand with fromDate : {fromDate} and toDate : {toDate}";
	public static final String TAXPERIOD_NOT_FOUND_FROMDATE = "{fromDate}";
	public static final String TAXPERIOD_NOT_FOUND_TODATE = "{toDate}";

	public static final String BUSINESSSERVICE_NOT_FOUND_KEY = "EG_BS_BUSINESSSERVICE_NOTFOUND";
	public static final String BUSINESSSERVICE_NOT_FOUND_MSG = "No Business Service masters found for give codes {resplaceValues}";
	public static final String BUSINESSSERVICE_NOT_FOUND_REPLACETEXT = "{resplaceValues}";

	public static final String TAXHEADS_NOT_FOUND_KEY = "EG_BS_TAXHEADS_NOTFOUND";
	public static final String TAXHEADS_NOT_FOUND_MSG = "No TaxHead masters found for give codes {resplaceValues}";
	public static final String TAXHEADS_NOT_FOUND_REPLACETEXT = "{resplaceValues}";
	
	public static final String USER_UUID_NOT_FOUND_KEY = "EG_BS_USER_UUID_NOTFOUND";
	public static final String USER_UUID_NOT_FOUND_MSG = "No users found for following uuids  {resplaceValues}";
	public static final String USER_UUID_NOT_FOUND_REPLACETEXT = "{resplaceValues}";
	
	public static final String INVALID_DEMAND_DETAIL_KEY = "EG_DEMAND_DEATIL_INVALID";
	public static final String INVALID_DEMAND_DETAIL_MSG = "Invalid demand details found with following Values : {resplaceValues}";
	public static final String INVALID_DEMAND_DETAIL_REPLACETEXT = "{resplaceValues}";
	
	public static final String INVALID_DEMAND_DETAIL_ERROR_MSG = "collection amount : {collection}, should not be greater than taxAmount : {tax}";
	public static final String INVALID_DEMAND_DETAIL_COLLECTION_TEXT = "{collection}";
	public static final String INVALID_DEMAND_DETAIL_TAX_TEXT = "{tax}";
	
	public static final String INVALID_NEGATIVE_DEMAND_DETAIL_ERROR_MSG = "collection amount : {collection}, should not be equal to 'ZERO' or tax amount : {tax} in case negative Tax demand detail";
	
	public static final String DEMAND_NOT_FOUND_KEY = "EG_BS_DEMANDS_NOT_FOUND";
	public static final String DEMAND_NOT_FOUND_MSG = "No Demands not found in db for following ids : {resplaceValues}";
	public static final String DEMAND_NOT_FOUND_REPLACETEXT = "{resplaceValues}";
	
	public static final String DEMAND_DETAIL_NOT_FOUND_KEY = "EG_BS_DEMAND_DETAIL_NOTFOUND";
	public static final String DEMAND_DETAIL_NOT_FOUND_MSG = "No Demand details found for the given ids :  {resplaceValues}";
	public static final String DEMAND_DETAIL_NOT_FOUND_REPLACETEXT = "{resplaceValues}";
	
	public static final String CONSUMER_CODE_DUPLICATE_KEY = "EG_BS_DUPLICATE_CONSUMERCODE";
	public static final String CONSUMER_CODE_DUPLICATE_MSG = "Demand already exists in the same period with the same businessService for the given consumercodes : {consumercodes}";
	public static final String CONSUMER_CODE_DUPLICATE_CONSUMERCODE_TEXT = "{consumercodes}";
}
