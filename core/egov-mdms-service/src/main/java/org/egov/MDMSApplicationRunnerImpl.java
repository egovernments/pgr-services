package org.egov;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Component
@Slf4j
public class MDMSApplicationRunnerImpl {

	@Autowired
	public ResourceLoader resourceLoader;

	@Value("${egov.mdms.conf.path}")
	public String mdmsFileDirectory;

	@Value("${masters.config.url}")
	public String masterConfigUrl;

	private  Map<String, Map<String, Map<String, JSONArray>>> tenantMap = new HashMap<>();

	private  Map<String, Map<String, Object>> masterConfigMap = new HashMap<>();

	ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void run() {
			log.info("Reading yaml files from: " + mdmsFileDirectory);
			readFiles(mdmsFileDirectory);
			readMdmsConfigFiles(masterConfigUrl);
			log.info("tenantMap1:" + tenantMap);
	}

	/***
	 *  Reads the files containing the master data from the given location 
	 *  and passes it to prepareTenantMap method
	 * @param baseFoderPath
	 */
	@SuppressWarnings("unchecked")
	public void readFiles(String baseFoderPath) {
		ObjectMapper jsonReader = new ObjectMapper();
		File folder = new File(baseFoderPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				log.info("File " + listOfFiles[i].getName());
				File file = listOfFiles[i];
				String name = file.getName();
				String[] fileName = name.split("[.]");
				if (fileName[fileName.length - 1].equals("json")) {
					log.debug("Reading json file....:- " + name);
					Map<String, Object> jsonMap;
					try {
						jsonMap = jsonReader.readValue(file, Map.class);
						prepareTenantMap(jsonMap);
						log.debug("json str:" + jsonMap);
					} catch (IOException e) {
						log.error(" exception occured while reading files : {}", e);
					}

				} else {
					log.info("file is not of a valid type please change and retry");
					log.info("Note: file can either be .yml/.yaml or .json");

				}

			} else if (listOfFiles[i].isDirectory()) {
				log.info("Directory " + listOfFiles[i].getName());
				readFiles(listOfFiles[i].getAbsolutePath());
			}
		}

	}

	/***
	 * Reads the master data from map<String, Object> and parses it into Map of masters in modules in tenants,
	 *  the parsed map is set into tenantMap.
	 * @param map
	 */
	@SuppressWarnings("unchecked")
	public void prepareTenantMap(Map<String, Object> map) {

		String tenantId = (String) map.get("tenantId");
		String moduleName = (String) map.get("moduleName");
		Set<String> masterKeys = map.keySet();
		String nonMasterKeys = "tenantId,moduleName";
		List<String> ignoreKey = new ArrayList<>(Arrays.asList(nonMasterKeys.split(",")));
		masterKeys.removeAll(ignoreKey);

		Map<String, JSONArray> masterDataMap = new HashMap<>();

		Iterator<String> masterKeyIterator = masterKeys.iterator();
		String masterName = null;
		JSONArray masterDataJsonArray = null;
		while (masterKeyIterator.hasNext()) {
			masterName = masterKeyIterator.next();

			try {
				masterDataJsonArray = JsonPath.read(objectMapper.writeValueAsString((List<Object>) map.get(masterName)),
						"$");
			} catch (JsonProcessingException e) {
				log.error(" exception occured while writing Value as String : {}", e);
			}
			masterDataMap.put(masterName, masterDataJsonArray);
		}

		if (!tenantMap.containsKey(tenantId)) {
			Map<String, Map<String, JSONArray>> moduleMap = new HashMap<>();
			moduleMap.put(moduleName, masterDataMap);
			tenantMap.put(tenantId, moduleMap);
		} else {
			Map<String, Map<String, JSONArray>> tenantModule = tenantMap.get(tenantId);

			if (!tenantModule.containsKey(moduleName)) {
				tenantModule.put(moduleName, masterDataMap);
			} else {
				Map<String, JSONArray> moduleMaster = tenantModule.get(moduleName);
				moduleMaster.putAll(masterDataMap);
				tenantModule.put(moduleName, moduleMaster);
			}

			tenantMap.put(tenantId, tenantModule);
		}

	}

	/***
	 *  Reads the configuration details of master data from the given URL
	 * @param masterConfigUrl
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void readMdmsConfigFiles(String masterConfigUrl) {
		ObjectMapper jsonReader = new ObjectMapper();

		log.info("GitHub Url TO Fetch MasterConfigs: " + masterConfigUrl);
		Map file = null;
		URL yamlFile = null;
		try {
			yamlFile = new URL(masterConfigUrl);
			file = jsonReader.readValue(new InputStreamReader(yamlFile.openStream()), Map.class);
		} catch (IOException e1) {
			log.error("Exception while fetching service map for: " + e1.getMessage());
		}
		masterConfigMap = file;
		log.info("the Master config Map : " + masterConfigMap);

	}

	/***
	 * 
	 * method returns the tenantMap which has been loaded at the time of application
	 * startup or refreshed during reload
	 * 
	 * @return tenantMap
	 */
	public  Map<String, Map<String, Map<String, JSONArray>>> getTenantMap() {
		return tenantMap;
	}
	
	/***
	 * method returns the MasterConfigMap which has been loaded at the time of application
	 * startup or refreshed during reload
	 * @return MasterConfigMap
	 */
	public  Map<String, Map<String, Object>> getMasterConfigMap() {
		return masterConfigMap;
	}

}
