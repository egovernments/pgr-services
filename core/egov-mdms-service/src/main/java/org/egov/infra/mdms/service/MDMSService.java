package org.egov.infra.mdms.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.MDMSApplicationRunnerImpl;
import org.egov.infra.mdms.utils.MDMSConstants;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class MDMSService {
	
	@Autowired 
	MDMSApplicationRunnerImpl applicationRunnerImpl;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${egov.kafka.topics.reload}")
	private String reloadTopic;

	/***
	 * returns the Master details in a map of module containing a map of master and list of data.
	 * @param mdmsCriteriaReq
	 * @return
	 */
	public Map<String, Map<String, JSONArray>> searchMaster(MdmsCriteriaReq mdmsCriteriaReq) {
		
		Map<String, Map<String, Map<String, JSONArray>>> tenantIdMap = applicationRunnerImpl.getTenantMap();

		String tenantId = mdmsCriteriaReq.getMdmsCriteria().getTenantId();

		Map<String, Map<String, JSONArray>> stateLevel = null;
		Map<String, Map<String, JSONArray>> ulbLevel = null;

		if (tenantId.contains(".")) {
			String[] array = tenantId.split("\\.");
			stateLevel = tenantIdMap.get(array[0]);
			ulbLevel = tenantIdMap.get(tenantId);
			if (ulbLevel == null)
				throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
		} else {
			stateLevel = tenantIdMap.get(tenantId);
			if (stateLevel == null)
				throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
		}

		List<ModuleDetail> moduleDetails = mdmsCriteriaReq.getMdmsCriteria().getModuleDetails();
		Map<String, Map<String, JSONArray>> responseMap = new HashMap<>();
		for (ModuleDetail moduleDetail : moduleDetails) {
			List<MasterDetail> masterDetails = moduleDetail.getMasterDetails();

			if (stateLevel != null || ulbLevel != null) {
				if (stateLevel != null && ulbLevel == null) {
					if (stateLevel.get(moduleDetail.getModuleName()) == null)
						continue;
				} else if (ulbLevel != null && stateLevel == null) {
					if (ulbLevel.get(moduleDetail.getModuleName()) == null)
						continue;
				}
				if (stateLevel != null || ulbLevel != null) {
					if (stateLevel.get(moduleDetail.getModuleName()) == null
							&& ulbLevel.get(moduleDetail.getModuleName()) == null)
						continue;
				}
			}

			Map<String, JSONArray> finalMasterMap = new HashMap<>();

			for (MasterDetail masterDetail : masterDetails) {
				JSONArray masterData = null;
				masterData = getMasterData(stateLevel, ulbLevel, moduleDetail.getModuleName(), masterDetail.getName());
				if (masterData == null)
					continue;

				if (masterDetail.getFilter() != null)
					masterData = filterMaster(masterData, masterDetail.getFilter());

				finalMasterMap.put(masterDetail.getName(), masterData);
			}
			responseMap.put(moduleDetail.getModuleName(), finalMasterMap);
		}
		return responseMap;
	}

	/***
	 *  prepares and returns master data from sets state level and ulb level data based on the given parameters.
	 * @param stateLevel
	 * @param ulbLevel
	 * @param moduleName
	 * @param masterName
	 * @return 
	 */
	private JSONArray getMasterData(Map<String, Map<String, JSONArray>> stateLevel,
			Map<String, Map<String, JSONArray>> ulbLevel, String moduleName, String masterName) {
		
		Map<String, Map<String, Object>> masterConfigMap = applicationRunnerImpl.getMasterConfigMap();

		Map<String, Object> moduleData = masterConfigMap.get(moduleName);
		log.info(" The Module Config map : {}",moduleData);
		Boolean isStateLevel = false;
		Object masterData = null;
		ObjectMapper mapper = new ObjectMapper();
		if (moduleData != null)
			masterData = moduleData.get(masterName);
		
		if (null != masterData) {
				try {
					isStateLevel = (Boolean) JsonPath.read(mapper.writeValueAsString(masterData),
							MDMSConstants.STATE_LEVEL_JSONPATH);
				} catch (JsonProcessingException e) {
					log.error(" exception while reading stateLevel field from masterConfig json : {}",e);
				}
		}
		log.info("MasterName... " + masterName + "isStateLevelConfiguration.." + isStateLevel);

		if (ulbLevel == null || isStateLevel) {
			if (stateLevel.get(moduleName) != null) {
				return stateLevel.get(moduleName).get(masterName);
			} else {
				return null;
			}
		} else if (ulbLevel.get(moduleName) != null) {
			return ulbLevel.get(moduleName).get(masterName);
		} else {
			return null;
		}
	}

	public JSONArray filterMaster(JSONArray masters, String filterExp) {
		JSONArray filteredMasters = JsonPath.read(masters, filterExp);
		return filteredMasters;
	}

	/***
	 * updates the cache in the service based on the given path
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	public void updateCache(String path) {

		ObjectMapper jsonReader = new ObjectMapper();

		try {
			URL jsonFileData = new URL(path);
			Map<String, Object> map = jsonReader.readValue(new InputStreamReader(jsonFileData.openStream()), Map.class);
			kafkaTemplate.send(reloadTopic, map);
		} catch (IOException e) {
			log.error(" exception in updateCache readValue from JsonFileData : {}",e);
			throw new CustomException("mdms_invalid_file_path", "invalid file path");
		}

	}

	public void reloadObj(Map<String, Object> map) {
		kafkaTemplate.send(reloadTopic, map);
	}
}
