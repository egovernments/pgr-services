package org.egov.wcms.util;

import java.util.ArrayList;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wcms.config.MainConfiguration;
import org.egov.wcms.web.models.SearcherRequest;
import org.egov.wcms.web.models.WaterConnectionRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WCServiceUtils {
	
	@Autowired
	private MainConfiguration mainConfiguration;
	
	@Autowired
	private ResponseInfoFactory responseInfoFactory;
	
	private final String searchNamePlaceholder = "{searchName}";
	
	public SearcherRequest getSearcherRequest(StringBuilder uri, Object request, RequestInfo requestInfo, String searchDefName) {
		uri.append(mainConfiguration.getEgovSeacherHost());
		String endPoint = mainConfiguration.getEgovSearcherEndpoint().replace(searchNamePlaceholder, searchDefName);
		uri.append(endPoint);
		
		return SearcherRequest.builder().requestInfo(requestInfo).searchCriteria(request).build();
		
	}
	
	/**
	 * Returns mapper with all the appropriate properties reqd in our functionalities.
	 * 
	 * @return ObjectMapper
	 */
	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
	}
	
	/**
	 * Default response is responseInfo with error status and empty lists
	 * 
	 * @param requestInfo
	 * @return ServiceResponse
	 */
	public WaterConnectionRes getDefaultWaterConnectionResponse(RequestInfo requestInfo) {
		return WaterConnectionRes.builder().responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfo, true))
		.connections(new ArrayList<>()).actionHistory(new ArrayList<>()).build();
	}
}
