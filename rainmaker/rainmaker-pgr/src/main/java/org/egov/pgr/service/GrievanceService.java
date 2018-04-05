package org.egov.pgr.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.pgr.PGRApp;
import org.egov.pgr.contract.CountResponse;
import org.egov.pgr.contract.IdResponse;
import org.egov.pgr.contract.RequestInfoWrapper;
import org.egov.pgr.contract.SearcherRequest;
import org.egov.pgr.contract.ServiceReqSearchCriteria;
import org.egov.pgr.contract.ServiceRequest;
import org.egov.pgr.contract.ServiceRequestDetails;
import org.egov.pgr.contract.ServiceResponse;
import org.egov.pgr.model.ActionHistory;
import org.egov.pgr.model.ActionInfo;
import org.egov.pgr.model.AuditDetails;
import org.egov.pgr.model.Service;
import org.egov.pgr.model.Service.StatusEnum;
import org.egov.pgr.producer.PGRProducer;
import org.egov.pgr.repository.FileStoreRepo;
import org.egov.pgr.repository.IdGenRepo;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.utils.ErrorConstants;
import org.egov.pgr.utils.PGRConstants;
import org.egov.pgr.utils.PGRUtils;
import org.egov.pgr.utils.ResponseInfoFactory;
import org.egov.pgr.utils.WorkFlowConfigs;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class GrievanceService {

	@Value("${kafka.topics.save.service}")
	private String saveTopic;

	@Value("${kafka.topics.update.service}")
	private String updateTopic;

	@Value("${kafka.topics.notification.complaint}")
	private String complaintTopic;

	@Value("${indexer.grievance.create}")
	private String indexerCreateTopic;

	@Value("${indexer.grievance.update}")
	private String indexerUpdateTopic;

	@Autowired
	private ResponseInfoFactory factory;

	@Autowired
	private IdGenRepo idGenRepo;

	@Autowired
	private PGRUtils pGRUtils;

	@Autowired
	private PGRProducer pGRProducer;

	@Autowired
	private FileStoreRepo fileStoreRepo;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
	
	//private static final String MODULE_NAME = "PGR:"; //FIXME need when businesskey becomes modulename:serviceRequestId

	/***
	 * Asynchronous method performs business logic if any and adds the data to
	 * persister queue on create topic
	 * 
	 * @param request
	 */
	public ServiceResponse create(ServiceRequest request) {

		Map<String, String> actionStatusMap = PGRApp.getActionStatusMap();

		log.debug(" the incoming request obj in service : {}", request);

		RequestInfo requestInfo = request.getRequestInfo();
		List<Service> serviceReqs = request.getServices();
		List<ActionInfo> actionInfos = request.getActionInfo();
		if(null==actionInfos)
			actionInfos = Arrays.asList(new ActionInfo[serviceReqs.size()]);
		String tenantId = serviceReqs.get(0).getTenantId();
		Integer servReqLen = serviceReqs.size();

		AuditDetails auditDetails = pGRUtils.getAuditDetails(String.valueOf(requestInfo.getUserInfo().getId()));

		String by = auditDetails.getCreatedBy() + ":" + requestInfo.getUserInfo().getRoles().get(0).getName();

		List<String> servReqIdList = getIdList(requestInfo, tenantId, servReqLen, PGRConstants.SERV_REQ_ID_NAME,
				PGRConstants.SERV_REQ_ID_FORMAT);

		for (int servReqCount = 0; servReqCount < servReqLen; servReqCount++) {

			Service servReq = serviceReqs.get(servReqCount);
			ActionInfo actionInfo = actionInfos.get(servReqCount);
			if (null == actionInfo) {
				actionInfo = new ActionInfo();
				actionInfos.add(actionInfo);
			}
			String currentId = servReqIdList.get(servReqCount);
			servReq.setAuditDetails(auditDetails);
			servReq.setServiceRequestId(currentId);
			servReq.setStatus(StatusEnum.OPEN);
			servReq.setFeedback(null);
			servReq.setRating(null);
			
			// FIXME TODO business key should be module name and currentid in future
			actionInfo.setUuid(UUID.randomUUID().toString());
			actionInfo.setBusinessKey(currentId);
			actionInfo.setBy(by);
			actionInfo.setWhen(auditDetails.getCreatedTime());
			actionInfo.setTenantId(tenantId);
			actionInfo.setStatus(actionStatusMap.get(WorkFlowConfigs.ACTION_OPEN));
			
		}
		request.setActionInfo(actionInfos);
		
		pGRProducer.push(saveTopic, request);
		return getServiceResponse(request);
	}

	/**
	 * Asynchronous method performs business logic if any and adds the data to
	 * persister queue on update topic
	 * 
	 * @param request
	 */
	public ServiceResponse update(ServiceRequest request) {

		log.debug(" the incoming request obj in service : {}", request);

		Map<String, List<String>> actioncurrentStatusMap = PGRApp.getActionCurrentStatusMap();
		Map<String, String> actionStatusMap = PGRApp.getActionStatusMap();
		Map<String, List<String>> errorMap = new HashMap<>();

		RequestInfo requestInfo = request.getRequestInfo();
		List<Service> serviceReqs = request.getServices();
		List<ActionInfo> actionInfos = request.getActionInfo();
		String tenantId = serviceReqs.get(0).getTenantId();

		final AuditDetails auditDetails = pGRUtils
				.getAuditDetails(String.valueOf(request.getRequestInfo().getUserInfo().getId()));
		String by = auditDetails.getCreatedBy() + ":" + requestInfo.getUserInfo().getRoles().get(0).getName();

		int serviceLen = serviceReqs.size();
		for (int index = 0; index < serviceLen; index++) {

			Service servReq = serviceReqs.get(index);
			ActionInfo actionInfo = null;
			if (!CollectionUtils.isEmpty(actionInfos))
				actionInfo = actionInfos.get(index);
			servReq.setAuditDetails(auditDetails);
			log.debug(" the action info : " + actionInfo);
			// FIXME TODO business key should be module name and currentid in future
			if (null != actionInfo) {
				actionInfo.setUuid(UUID.randomUUID().toString());
				actionInfo.setBusinessKey(servReq.getServiceRequestId());
				actionInfo.setBy(by);
				actionInfo.setWhen(auditDetails.getCreatedTime());
				actionInfo.setTenantId(tenantId);
				actionInfo.setStatus(actionInfo.getAction());
				if (null != actionInfo.getAction() && actionStatusMap.get(actionInfo.getAction()) != null) {
					System.err.println(" the is blaock : "+(WorkFlowConfigs.ACTION_CLOSE.equals(actionInfo.getAction()) && (null!=servReq.getFeedback()||null!=servReq.getRating())));
					if (WorkFlowConfigs.ACTION_CLOSE.equals(actionInfo.getAction()) && (null!=servReq.getFeedback()||null!=servReq.getRating()))
						addError(ErrorConstants.UPDATE_FEEDBACK_ERROR_MSG + actionInfo.getAction() + ", with service Id : "+servReq.getServiceRequestId(),
								ErrorConstants.UPDATE_FEEDBACK_ERROR_KEY, errorMap);
					if (isUpdateValid(requestInfo, actionInfo, actioncurrentStatusMap.get(actionInfo.getAction()))) {
						String resultStatus = actionStatusMap.get(actionInfo.getAction());
						actionInfo.setStatus(resultStatus);
						servReq.setStatus(StatusEnum.valueOf(resultStatus));
						servReq.setStatus(StatusEnum.fromValue(resultStatus));
					} else {

						String errorMsg = " The Given Action " + actionInfo.getAction()
								+ "cannot be applied for the Current status of the Grievance with ServiceRequestId "
								+ servReq.getServiceRequestId();
						addError(errorMsg, ErrorConstants.UPDATE_ERROR_KEY, errorMap);
					}
				} else if (null != actionInfo.getAction()) {
					String errorMsg = " The Given Action " + actionInfo.getAction() + " is invalid ";
					addError(errorMsg, ErrorConstants.UPDATE_ERROR_KEY, errorMap);
				}
			}/* else {
				addError(ErrorConstants.UPDATE_FEEDBACK_ERROR_MSG_NO_ACTION + ", with service Id : "
						+ servReq.getServiceRequestId(), ErrorConstants.UPDATE_FEEDBACK_ERROR_KEY, errorMap);
			}*/
		}
		
		if(!errorMap.isEmpty()) {
			Map<String, String> newMap = new HashMap<>();
			errorMap.keySet().forEach(key -> newMap.put(key,errorMap.get(key).toString()));
			throw new CustomException(newMap);
		}

		pGRProducer.push(updateTopic, request);
		return getServiceResponse(request);
	}

	private void addError(String errorMsg, String key, Map<String, List<String>> errorMap) {

		List<String> errors = errorMap.get(key);
		if (null == errors) {
			errors = Arrays.asList(errorMsg);
			errorMap.put(key, errors);
		} else
			errors.add(errorMsg);
	}

	private boolean isUpdateValid(RequestInfo requestInfo, ActionInfo actionInfo, List<String> currentStatusList) {

		log.info(" the current list possible : "+ currentStatusList);
		ServiceReqSearchCriteria serviceReqSearchCriteria = ServiceReqSearchCriteria.builder()
				.tenantId(actionInfo.getTenantId()).serviceRequestId(Arrays.asList(actionInfo.getBusinessKey()))
				.build();

		List<ActionInfo> infos = ((ServiceResponse) getServiceRequestDetails(requestInfo, serviceReqSearchCriteria))
				.getActionHistory().get(0).getActions();
		
		
		for (int i = 0;i <= infos.size()-1; i++) {
				String status = infos.get(i).getStatus();
				log.info(" the status is : "+ status);
				if (null != status) {
					log.info(" is it true : "+ currentStatusList.contains(status));
					return currentStatusList.contains(status);
				}
			}
		return false;
	}


	/**
	 * method to parse the IdGenResponse from IdgenRepo to List of String ids
	 * required by the respective methods
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param length
	 * @param idKey
	 * @param idformat
	 * 
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, Integer length, String idKey,
			String idformat) {

		return idGenRepo.getId(requestInfo, tenantId, length, idKey, idformat).getIdResponses().stream()
				.map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * returns ServiceResponse built based on the given ServiceRequest
	 * 
	 * @param serviceReqRequest
	 * @return serviceReqResponse
	 */
	public ServiceResponse getServiceResponse(ServiceRequest serviceReqRequest) {		
		// for loop is to produce lag for fetching data
		for (int i = 0; i <= 100000; i++) {
			i++;
			i--;
		}
		ObjectMapper mapper = pGRUtils.getObjectMapper();
		List<Service> services = serviceReqRequest.getServices();
		String tenantId = services.get(0).getTenantId();
		List<String> serviceRequestIds = services.parallelStream().map(Service::getServiceRequestId)
				.collect(Collectors.toList());

		ServiceReqSearchCriteria serviceReqSearchCriteria = ServiceReqSearchCriteria.builder()
				.serviceRequestId(serviceRequestIds).tenantId(tenantId).build();

		ServiceResponse serviceResponse = null;
		/*
		 * 	for loop is to produce lag for fetching data.
		 *  since there might be a lag in kafka and persiter to persist
		 *  to get the best result we are inducing this lag
		 */
			for (int i = 0; i <= 100000; i++) {
				i++;
				i--;
			}
			Object response = getServiceRequestDetails(serviceReqRequest.getRequestInfo(), serviceReqSearchCriteria);
			serviceResponse = mapper.convertValue(response, ServiceResponse.class);

		if (CollectionUtils.isEmpty(serviceResponse.getActionHistory()))
			return ServiceResponse.builder()
					.responseInfo(factory.createResponseInfoFromRequestInfo(serviceReqRequest.getRequestInfo(), true))
					.services(serviceReqRequest.getServices())
					.actionHistory(convertActionInfosToHistorys(serviceReqRequest.getActionInfo())).build();
		
		return serviceResponse;
	}
	
	private List<ActionHistory> convertActionInfosToHistorys(List<ActionInfo> actionInfos) {

		List<ActionHistory> historys = new ArrayList<>();

		actionInfos.forEach(a -> {
			List<ActionInfo> infos = new ArrayList<>();
			infos.add(a);
			historys.add(new ActionHistory(infos));
		});
		return historys;
	}

	/**
	 * Method to return service requests along with details acc to V5 design
	 * received from the repo to the controller in the reqd format
	 * 
	 * @param requestInfo
	 * @param serviceReqSearchCriteria
	 * @return ServiceReqResponse
	 * @author vishal
	 */
	public Object getServiceRequestDetails(RequestInfo requestInfo, ServiceReqSearchCriteria serviceReqSearchCriteria) {
		StringBuilder uri = new StringBuilder();
		SearcherRequest searcherRequest = null;
		try {
			enrichRequest(requestInfo, serviceReqSearchCriteria);
		} catch (CustomException e) {
			if (e.getMessage().equals(ErrorConstants.NO_DATA_MSG))
				return pGRUtils.getDefaultServiceResponse(requestInfo);
			else
				throw e;
		}
		searcherRequest = pGRUtils.prepareSearchRequestWithDetails(uri, serviceReqSearchCriteria, requestInfo);
		Object response = serviceRequestRepository.fetchResult(uri, searcherRequest);
		log.debug(PGRConstants.SEARCHER_RESPONSE_TEXT + response);
		if (null == response)
			return pGRUtils.getDefaultServiceResponse(requestInfo);
		return prepareResult(response, requestInfo);
	}
	
	public void enrichRequest(RequestInfo requestInfo, ServiceReqSearchCriteria serviceReqSearchCriteria) {
		log.info("Enriching request.........: " + serviceReqSearchCriteria);
		if (requestInfo.getUserInfo().getRoles().get(0).getName().equals("DGRO")) {
			Integer departmenCode = getDepartmentCode(serviceReqSearchCriteria, requestInfo);
			String department = getDepartment(serviceReqSearchCriteria, requestInfo, departmenCode);
			Object response = fetchServiceCodes(requestInfo, serviceReqSearchCriteria.getTenantId(), department);
			if (null == response) {
				log.error("Searcher returned zero serviceCodes for dept: " + department);
				throw new CustomException(ErrorConstants.NO_DATA_KEY, ErrorConstants.NO_DATA_MSG);
			}
			try {
				List<String> serviceCodes = JsonPath.read(response, PGRConstants.JSONPATH_SERVICE_CODES);
				serviceReqSearchCriteria.setServiceCodes(serviceCodes);
			} catch (Exception e) {
				log.error("Exception while parsing serviceCodes: ", e);
				throw new CustomException(ErrorConstants.NO_DATA_KEY, ErrorConstants.NO_DATA_MSG);
			}
		} else if (requestInfo.getUserInfo().getRoles().get(0).getName().equalsIgnoreCase("CITIZEN")) {
			serviceReqSearchCriteria.setAccountId(requestInfo.getUserInfo().getId().toString());
			String[] tenant = serviceReqSearchCriteria.getTenantId().split("[.]");
			if (tenant.length > 1)
				serviceReqSearchCriteria.setTenantId(tenant[0]);
		}
		if (requestInfo.getUserInfo().getRoles().get(0).getName().equalsIgnoreCase("EMPLOYEE")
				&& (null != serviceReqSearchCriteria.getAssignedTo() && !serviceReqSearchCriteria.getAssignedTo().isEmpty())) {
			serviceReqSearchCriteria.setAssignedTo(requestInfo.getUserInfo().getId().toString());
		}
		if (null != serviceReqSearchCriteria.getAssignedTo() && !serviceReqSearchCriteria.getAssignedTo().isEmpty()) {
			List<String> serviceRequestIds = getServiceRequestIdsOnAssignedTo(requestInfo, serviceReqSearchCriteria);
			if (serviceRequestIds.isEmpty())
				throw new CustomException("400", "No Data");
			serviceReqSearchCriteria.setServiceRequestId(serviceRequestIds);
		}
		
		log.info("Enriched request: " + serviceReqSearchCriteria);

	}

	public Integer getDepartmentCode(ServiceReqSearchCriteria serviceReqSearchCriteria, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder();
		RequestInfoWrapper requestInfoWrapper = pGRUtils.prepareRequestForEmployeeSearch(uri, requestInfo,
				serviceReqSearchCriteria);
		Object response = null;
		log.debug("Employee: " + response);
		Integer departmenCode = null;
		try {
			response = serviceRequestRepository.fetchResult(uri, requestInfoWrapper);
			if (null == response) {
				throw new CustomException(ErrorConstants.UNAUTHORIZED_EMPLOYEE_TENANT_KEY,
						ErrorConstants.UNAUTHORIZED_EMPLOYEE_TENANT_MSG);
			}
			log.debug("Employee: " + response);
			departmenCode = JsonPath.read(response, PGRConstants.EMPLOYEE_DEPTCODE_JSONPATH);
		} catch (Exception e) {
			log.error("Exception: " + e);
			throw new CustomException(ErrorConstants.UNAUTHORIZED_EMPLOYEE_TENANT_KEY,
					ErrorConstants.UNAUTHORIZED_EMPLOYEE_TENANT_MSG);
		}
		return departmenCode;
	}

	public String getDepartment(ServiceReqSearchCriteria serviceReqSearchCriteria, RequestInfo requestInfo,
			Integer departmentCode) {
		StringBuilder deptUri = new StringBuilder();
		String department = null;
		Object response = null;
		RequestInfoWrapper requestInfoWrapper = pGRUtils.prepareRequestForDeptSearch(deptUri, requestInfo,
				departmentCode, serviceReqSearchCriteria.getTenantId());
		try {
			response = serviceRequestRepository.fetchResult(deptUri, requestInfoWrapper);
			if (null == response) {
				throw new CustomException(ErrorConstants.INVALID_DEPARTMENT_TENANT_KEY,
						ErrorConstants.INVALID_DEPARTMENT_TENANT_MSG);
			}
			department = JsonPath.read(response, PGRConstants.DEPARTMENTNAME_EMPLOYEE_JSONPATH);
		} catch (Exception e) {
			log.error("Exception: " + e);
			throw new CustomException(ErrorConstants.INVALID_DEPARTMENT_TENANT_KEY,
					ErrorConstants.INVALID_DEPARTMENT_TENANT_MSG);
		}
		return department;
	}
	
	/**
	 * method to fetch service codes from mdms based on dept
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param department
	 * @return Object
	 * @author vishal
	 */
	public Object fetchServiceCodes(RequestInfo requestInfo, String tenantId, String department) {
		StringBuilder uri = new StringBuilder();
		MdmsCriteriaReq mdmsCriteriaReq = pGRUtils.prepareSearchRequestForServiceCodes(uri, tenantId, department,
				requestInfo);
		Object response = null;
		try {
			response = serviceRequestRepository.fetchResult(uri, mdmsCriteriaReq);
		} catch (Exception e) {
			log.error("Exception while fetching serviceCodes: " + e);
		}
		return response;

	}
	
	/**
	 * Method to return service requests ids based on the assignedTo
	 * 
	 * @param requestInfo
	 * @param serviceReqSearchCriteria
	 * @return List<String>
	 * @author vishal
	 */
	public List<String> getServiceRequestIdsOnAssignedTo(RequestInfo requestInfo,
			ServiceReqSearchCriteria serviceReqSearchCriteria) {
		StringBuilder uri = new StringBuilder();
		List<String> serviceRequestIds = new ArrayList<>();
		SearcherRequest searcherRequest = pGRUtils.prepareSearchRequestForAssignedTo(uri, serviceReqSearchCriteria,
				requestInfo);
		try {
			Object response = serviceRequestRepository.fetchResult(uri, searcherRequest);
			log.debug("Searcher response: " + response);
			if (null == response)
				return serviceRequestIds;
			serviceRequestIds = JsonPath.read(response, PGRConstants.SRID_ASSIGNEDTO_JSONPATH);
		} catch (Exception e) {
			log.error("Exception while parsing SRid search on AssignedTo result: " + e);
			return serviceRequestIds;
		}

		log.debug("serviceRequestIds: " + serviceRequestIds);

		return serviceRequestIds;

	}
	
	
	public ServiceResponse prepareResult(Object response, RequestInfo requestInfo) {
		ObjectMapper mapper = pGRUtils.getObjectMapper();
		List<Service> services = new ArrayList<>();
		List<ActionHistory> actionHistory = new ArrayList<>();
		
		List<ServiceRequestDetails> result = new ArrayList<>();
		List<Object> list = JsonPath.read(response, "$.services");
		for (Object entry : list) {
			ServiceRequestDetails object = mapper.convertValue(entry, ServiceRequestDetails.class);
			result.add(object);
			log.debug("Object: " + object);
		}
		for (ServiceRequestDetails obj : result) {
			List<ActionInfo> action = obj.getActionhistory();
			ActionHistory actionHis = new ActionHistory();
			actionHis.setActions(action);
			actionHistory.add(actionHis);

			obj.setActionhistory(null);
			services.add(obj.getServices());
		}
		replaceIdsWithUrls(actionHistory);

		return ServiceResponse.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
				.services(services).actionHistory(actionHistory).build();
	}


	/**
	 * Fetches count of service requests and returns in the reqd format.
	 * 
	 * @param requestInfo
	 * @param serviceReqSearchCriteria
	 * @return Object
	 * @author vishal
	 */
	public Object getCount(RequestInfo requestInfo, ServiceReqSearchCriteria serviceReqSearchCriteria) {
		StringBuilder uri = new StringBuilder();
		SearcherRequest searcherRequest = null;
		try {
			enrichRequest(requestInfo, serviceReqSearchCriteria);
		} catch (CustomException e) {
			if (e.getMessage().equals(ErrorConstants.NO_DATA_MSG))
				return pGRUtils.getDefaultCountResponse(requestInfo);
			else
				throw e;
		}
		if(null != serviceReqSearchCriteria.getAssignedTo() && !serviceReqSearchCriteria.getAssignedTo().isEmpty()) {
			return new CountResponse(factory.createResponseInfoFromRequestInfo(requestInfo, true), Double.valueOf(serviceReqSearchCriteria.getServiceRequestId().size()));
		}
		searcherRequest = pGRUtils.prepareCountRequestWithDetails(uri, serviceReqSearchCriteria, requestInfo);
		Object response = serviceRequestRepository.fetchResult(uri, searcherRequest);
		log.info("Searcher response: " + response);
		if (null == response)
			return pGRUtils.getDefaultServiceResponse(requestInfo);
		Double count = JsonPath.read(response, PGRConstants.PG_JSONPATH_COUNT);
		return new CountResponse(factory.createResponseInfoFromRequestInfo(requestInfo, true), count);
	}
		

	/**
	 * method to replace the fileStoreIds with the respective urls acquired from
	 * filestore service
	 * 
	 * @param historyList
	 */
	private void replaceIdsWithUrls(List<ActionHistory> historyList) {

		if (CollectionUtils.isEmpty(historyList))
			return;
		try {
			String tenantId = historyList.get(0).getActions().get(0).getTenantId();
			List<String> fileStoreIds = new ArrayList<>();
	
			historyList.forEach(history -> history.getActions().forEach(action -> {
				List<String> media = action.getMedia();
				if (!CollectionUtils.isEmpty(media))
					fileStoreIds.addAll(media);
			}));
	
			Map<String, String> urlIdMap = null;
			try {
			urlIdMap = fileStoreRepo.getUrlMaps(tenantId, fileStoreIds);
			}catch (Exception e) {
				log.error(" exception while connecting to filestore : "+e);
			}
	
			log.info("urlIdMap: " + urlIdMap);
			if (null != urlIdMap) {
				for (int i = 0; i < historyList.size(); i++) {
					ActionHistory history = historyList.get(i);
					for (int j = 0; j < history.getActions().size(); j++) {
						List<ActionInfo> actionList = history.getActions();
						ActionInfo info = actionList.get(j);
						if(null == info.getMedia())
							break;
						List<String> mediaList = new ArrayList<>();
						for (int k = 0; k < info.getMedia().size(); k++) {
							List<String> oldMedia = info.getMedia();
							String fileStoreId = oldMedia.get(k);
							String url = urlIdMap.get(fileStoreId);
							if (null != url)
								mediaList.add(url);
							else
								mediaList.add(fileStoreId);
						}
						info.setMedia(mediaList);
					}
				}
			}
		}catch(Exception e) {
			log.error("Exception while replacing s3 links: "+e);
		}
	}
}
