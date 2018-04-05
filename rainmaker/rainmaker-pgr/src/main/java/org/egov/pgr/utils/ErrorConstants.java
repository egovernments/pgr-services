package org.egov.pgr.utils;

public class ErrorConstants {

	private ErrorConstants() {}
	
	// all the keys should standard and complaint for localization
	
	public static final String NO_DATA_MSG = "No Data";
	public static final String NO_DATA_KEY = "External data not found";
	
	public static final String UNAUTHORIZED_EMPLOYEE_TENANT_MSG = "The Given Employee is Unauthorized for this tenant";
	public static final String UNAUTHORIZED_EMPLOYEE_TENANT_KEY = "Unauthorized Employee";
	
	public static final String INVALID_DEPARTMENT_TENANT_MSG = "The Given Department is Invalid for this tenant";
	public static final String INVALID_DEPARTMENT_TENANT_KEY = "Invalid Department";
	
	public static final String INVALID_TENANT_ID_MDMS_SERVICE_CODE_KEY = "EG_PGR_MDMS_SERVICE_CODES";
	public static final String INVALID_TENANT_ID_MDMS_SERVICE_CODE_MSG = "No serviceDefs data found for this tenant";
	
	public static final String UPDATE_ERROR_KEY = "EG_PGR_INVALID_ACTION_UPDATE"; 
	public static final String UPDATE_FEEDBACK_ERROR_MSG = "Feedback and Rating cannot be updated for current action -"; 

	public static final String UPDATE_FEEDBACK_ERROR_MSG_NO_ACTION = "Feedback and Rating can be updated only for action -close"; 
	public static final String UPDATE_FEEDBACK_ERROR_KEY = "EG_PGR_UPDATE_RATING_FEEDBACK_NOT_APPLICABLE";
	
	public static final String UNEQUAL_REQUEST_SIZE_KEY = "EG_PGR_REQUEST_UNEQUAL_SIZES";
	public static final String UNEQUAL_REQUEST_SIZE_MSG = "Services and ActionInfo must be of same size";
	
	
	public static final String CREATE_ADDRESS_COMBO_ERROR_KEY = "EG_PGR_CREATE_ADDRESS_ERROR";
	public static final String CREATE_ADDRESS_COMBO_ERROR_MSG = "Any one of the combinations of (address or addressId or lat/long) must be provided in the Grievance";
	
	public static final String ASSIGNEE_MISSING_FOR_ACTION_ASSIGN_REASSIGN_KEY = "EG_PGR_UPDATE_ASSIGN_REASSIGN";
	public static final String ASSIGNEE_MISSING_FOR_ACTION_ASSIGN_REASSIGN_MSG = "The assignees are missing for the assign actions of services with ids : ";
	
	public static final String ASSIGNEE_NOT_ALLOWED_KEY = "EG_PGR_UPDATE_ASSIGNEE_NOT_ALLOWED";
	public static final String ASSIGNEE_NOT_ALLOWED_MSG = "The assignees are not applicable for the following services due to the nature of their actions : ";
	
	
	//public static final String UPDATE_NO_ACTIONS_ERROR_KEY = "EG_PGR_UPDATE_ACTIONINFO";
	
	//public static final String UPDATE_NO_ACTIONS_ERROR_MSG = "Update cannot be performed without";
}
