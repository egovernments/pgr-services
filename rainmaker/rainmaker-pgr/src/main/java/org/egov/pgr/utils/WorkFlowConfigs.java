package org.egov.pgr.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *  Utility Class to keep all the values related to workflow
 *  
 * @author kavi elrey
 *
 */
public class WorkFlowConfigs {

	public static final String ROLE_EMPLOYEE = "EMPLOYEE";
	public static final String ROLE_CITIZEN = "CITIZEN";
	public static final String ROLE_GRO = "GRO";
	public static final String ROLE_DGRO = "DGRO";

	public static final String ACTION_OPEN = "open";
	public static final String ACTION_ASSIGN = "assign";
	public static final String ACTION_CLOSE = "close";
	public static final String ACTION_REJECT = "reject";
	public static final String ACTION_RESOLVE = "resolve";
	public static final String ACTION_REOPEN = "reopen";
	public static final String ACTION_REASSIGN = "reassign";
	public static final String ACTION_REQUEST_FOR_REASSIGN = "requestforreassign";

	public static final String STATUS_OPENED = "open";
	public static final String STATUS_ASSIGNED = "assigned";
	public static final String STATUS_CLOSED = "closed";
	public static final String STATUS_REJECTED = "rejected";
	public static final String STATUS_RESOLVED = "resolved";
	public static final String STATUS_REASSIGN_REQUESTED = "reassignrequested";
	
	private WorkFlowConfigs() {}

	private static Map<String, String> actionStatusMap = prepareStatusMap();

	private static Map<String, List<String>> actionCurrentStatusMap = prepareActionCurrentStatusMap();

	private static Map<String, List<String>> roleActionMap = prepareRoleActionMap();

	private static Map<String, String> prepareStatusMap() {

		Map<String, String> map = new HashMap<>();
		map.put(ACTION_OPEN, STATUS_OPENED);
		map.put(ACTION_ASSIGN, STATUS_ASSIGNED);
		map.put(ACTION_CLOSE, STATUS_CLOSED);
		map.put(ACTION_REJECT, STATUS_REJECTED);
		map.put(ACTION_RESOLVE, STATUS_RESOLVED);
		map.put(ACTION_REOPEN, STATUS_OPENED);
		map.put(ACTION_REASSIGN, STATUS_ASSIGNED);
		map.put(ACTION_REQUEST_FOR_REASSIGN, STATUS_REASSIGN_REQUESTED);
		return map;
	}

	private static Map<String, List<String>> prepareActionCurrentStatusMap() {

		Map<String, List<String>> map = new HashMap<>();
		map.put(ACTION_ASSIGN, Arrays.asList(STATUS_OPENED));
		map.put(ACTION_CLOSE, Arrays.asList(STATUS_REJECTED, STATUS_RESOLVED));
		map.put(ACTION_REJECT, Arrays.asList(STATUS_ASSIGNED, STATUS_OPENED , STATUS_REASSIGN_REQUESTED));
		map.put(ACTION_RESOLVE, Arrays.asList(STATUS_ASSIGNED));
		map.put(ACTION_REOPEN, Arrays.asList(STATUS_REJECTED, STATUS_RESOLVED));
		map.put(ACTION_REASSIGN, Arrays.asList(STATUS_OPENED, STATUS_ASSIGNED , STATUS_REASSIGN_REQUESTED));
		map.put(ACTION_REQUEST_FOR_REASSIGN, Arrays.asList(STATUS_ASSIGNED));
		return map;
	}

	private static Map<String, List<String>> prepareRoleActionMap() {

		Map<String, List<String>> map = new HashMap<>();
		map.put(ROLE_EMPLOYEE, Arrays.asList(ACTION_RESOLVE, ACTION_REQUEST_FOR_REASSIGN));
		map.put(ROLE_CITIZEN, Arrays.asList(ACTION_OPEN, ACTION_CLOSE, ACTION_REOPEN));
		map.put(ROLE_GRO, Arrays.asList(ACTION_ASSIGN, ACTION_REJECT, ACTION_REASSIGN));
		map.put(ROLE_DGRO, Arrays.asList(ACTION_ASSIGN, ACTION_REJECT, ACTION_REASSIGN));
		return map;
	}
	
	public static Map<String, String> getActionStatusMap(){
		return actionStatusMap;
	}
	
	public static Map<String, List<String>> getActionCurrentStatusMap(){
		return actionCurrentStatusMap;
	}
	
	public static Map<String, List<String>> getRoleActionMap(){
		return roleActionMap;
	}
}
