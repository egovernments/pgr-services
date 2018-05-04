package org.egov.wcms.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.tracer.model.CustomException;
import org.egov.wcms.service.WaterConnectionService;
import org.egov.wcms.web.models.Connection;
import org.egov.wcms.web.models.WaterConnectionReq;
import org.egov.wcms.web.models.WaterConnectionRes;
import org.egov.wcms.web.models.WaterConnectionSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WaterConnectionValidator {
	
	@Autowired
	private WaterConnectionService waterConnectionService;

	/**
	 * Validates create request by performing the following validations:
	 * 
	 * @param connections
	 */
	public void validateCreateRequest(WaterConnectionReq connections) {
		
	}
	
	/**
	 * Validates update by performing the following validations:
	 * 1. All records must have same tenantId.
	 * 2. Check if the records exist in the db, otherwise, throw exception.
	 * 
	 * @param connections
	 */
	public void validateUpdateRequest(WaterConnectionReq connections) {
	    Set<String> tenantIds = connections.getConnections().parallelStream()
				.map(Connection::getTenantId).collect(Collectors.toSet());
		//(1)
		if(tenantIds.size() > 1)
			throw new CustomException("MULTIPLE_TENANTS", "All records must have same tenantId");
		//
		
		List<String> connectionNumbers = connections.getConnections().parallelStream()
				.map(Connection::getConnectionNumber).collect(Collectors.toList());
		if(connectionNumbers.isEmpty())
			throw new CustomException("INVALID UPDATE_REQUEST", "Atleast one connection must be present");
		
		WaterConnectionSearchCriteria waterConnectionSearchCriteria = WaterConnectionSearchCriteria.builder()
				.connectionNumber(connectionNumbers).tenantId(connections.getConnections().get(0).getTenantId()).build();
		WaterConnectionRes waterConnectionRes = waterConnectionService.getWaterConnections(waterConnectionSearchCriteria, connections.getRequestInfo());
		List<String> invalidConnections = new ArrayList<>();
		
		//(2)
		if(waterConnectionRes.getConnections().size() != connectionNumbers.size()) {
			List<String> searchResultconnectionNumbers = waterConnectionRes.getConnections().parallelStream()
					.map(Connection::getConnectionNumber).collect(Collectors.toList());
			for(String connection: connectionNumbers) {
				if(!searchResultconnectionNumbers.contains(connection))
					invalidConnections.add(connection);
			}
			throw new CustomException("INVALID_UPDATE_REQUEST", "Following connections do not exist: "+invalidConnections);
		}	
		//
	}
	
	/**
	 * Validates search request performing the following validatons:
	 * 
	 * @param WaterConnectionSearchCriteria
	 */
	public void validateSearchRequest(WaterConnectionSearchCriteria WaterConnectionSearchCriteria) {
		
	}
}
