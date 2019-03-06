package org.egov.demand.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 
 * @author kavi elrey
 *
 * Generic Repository to make a rest call and return JSON response 
 */
@Repository
@Slf4j
public class ServiceRequestRepository {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RestTemplate restTemplate;


	/**
	 * fetch method which takes the URI and request object
	 *  and returns response in generic format
	 * @param uri
	 * @param request
	 * @return
	 */
	public DocumentContext fetchResult(String uri, Object request) {
		
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Map response = null;
		log.info("URI: "+uri.toString());
		
		try {
			log.info("Request: "+mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		}catch(HttpClientErrorException e) {
			log.error("External Service threw an Exception: ",e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		}catch(Exception e) {
			log.error("Exception while fetching from searcher: ",e);
		}

		return JsonPath.parse(response);
	}
}
