package org.egov.wcms.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wcms.config.MainConfiguration;
import org.egov.wcms.producer.WaterConnectionProducer;
import org.egov.wcms.repository.WCRepository;
import org.egov.wcms.util.WCServiceUtils;
import org.egov.wcms.util.WaterConnectionConstants;
import org.egov.wcms.web.models.SearcherRequest;
import org.egov.wcms.web.models.WaterConnectionReq;
import org.egov.wcms.web.models.WaterConnectionRes;
import org.egov.wcms.web.models.WaterConnectionSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class WaterConnectionService {
	
	@Autowired
	private WCServiceUtils wCServiceUtils;
	
	@Autowired
	private WCRepository wcRepository;
	
	@Autowired
	private WaterConnectionProducer waterConnectionProducer;
	
	@Autowired
	private MainConfiguration mainConfiguration;
	
	/**
	 * Searches Water Connections based on the criteria in WaterConnectionSearchCriteria.
	 * 
	 * @param WaterConnectionSearchCriteria
	 * @param requestInfo
	 * @return WaterConnectionRes
	 */
	public WaterConnectionRes getWaterConnections(WaterConnectionSearchCriteria WaterConnectionSearchCriteria, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder();
		Object response = null;
		WaterConnectionRes waterConnectionRes = null;
		ObjectMapper mapper = wCServiceUtils.getObjectMapper();
		SearcherRequest searcherRequest = wCServiceUtils.getSearcherRequest(uri, WaterConnectionSearchCriteria, requestInfo, WaterConnectionConstants.SEARCHER_WC_SEARCH_DEF_NAME);
		try {
			response = wcRepository.fetchResult(uri, searcherRequest);
			if(null == response) {
				return wCServiceUtils.getDefaultWaterConnectionResponse(requestInfo);
			}
			waterConnectionRes = mapper.convertValue(response, WaterConnectionRes.class);
		}catch(Exception e) {
			return wCServiceUtils.getDefaultWaterConnectionResponse(requestInfo);
		}
		return waterConnectionRes;
	}
	
	/**
	 * Creates water connection in the postgres db through persister and pushes the same to elasticsearch through indexer. Both consumers pick from the same topic.
	 * @param connections
	 * @return WaterConnectionRes
	 */
	public WaterConnectionRes createWaterConnections(WaterConnectionReq connections) {
		enrichCreateRequest(connections);
		waterConnectionProducer.push(mainConfiguration.getSaveWaterConnectionTopic(), connections);
		return null;
	}
	
	/**
	 * Request enrichment for create flow based on the following use-cases:
	 * 1. Generate ids for each request using idGen. 
	 * @param connections
	 */
	public void enrichCreateRequest(WaterConnectionReq connections) {
		
	}
	
	/**
	 * Updates the water connection in postgres db through persister and pushes the same to elasticsearch through indexer. Both consumers pick from the same topic.
	 * @param connections
	 * @return
	 */
	public WaterConnectionRes updateWaterConnections(WaterConnectionReq connections) {
		enrichUpdateRequest(connections);
		waterConnectionProducer.push(mainConfiguration.getUpdateWaterConnectionTopic(), connections);
		return null;
	}

	/**
	 * Request enrichment for update flow based on the following use-cases:
	 * 
	 * @param connections
	 */
	public void enrichUpdateRequest(WaterConnectionReq connections) {
		
	}

}
