package org.egov.mdms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.receipt.consumer.model.BusinessService;
import org.egov.receipt.consumer.model.FilterRequest;
import org.egov.receipt.consumer.model.MasterDetail;
import org.egov.receipt.consumer.model.MdmsCriteria;
import org.egov.receipt.consumer.model.MdmsCriteriaReq;
import org.egov.receipt.consumer.model.ModuleDetail;
import org.egov.receipt.consumer.model.RequestInfo;
import org.egov.receipt.consumer.model.TaxHeadMaster;
import org.egov.receipt.consumer.model.VoucherIntegrationLogTO;
import org.egov.receipt.custom.exception.VoucherCustomException;
import org.egov.reciept.consumer.config.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@Service
public class MicroServiceUtilImpl implements MicroServiceUtil{
	private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceUtilImpl.class);
	@Autowired
	private TokenService tokenService;
	@Autowired
	private PropertiesManager manager;
	@Autowired
	private ModuleDetail moduleDetail;
	@Autowired
	private MasterDetail masterDetails;
	@Autowired
	private RequestInfo requestInfo;
	@Autowired
	private MdmsCriteria mdmscriteria;
	@Autowired
	private MdmsCriteriaReq mdmsrequest;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ObjectMapper mapper;
	@Autowired
	private VoucherIntegrationLogTO voucherIntegrationLogTO;
	
	@Override
	public List<BusinessService> getBusinessService(String tenantId, String code) throws VoucherCustomException{
		FilterRequest filter = new FilterRequest();
		filter.setCode(code);
		Object ptMdmsData = this.getPTMdmsData(tenantId, "FinanceModule", "BusinessServiceMapping", filter);
		if(LOGGER.isInfoEnabled())
			LOGGER.info("ptMdmsData  :::  "+ptMdmsData);
		List<BusinessService> list = new ArrayList<>();;
		try {
			if(ptMdmsData != null){
				list = mapper.convertValue(JsonPath.read(ptMdmsData, "$.MdmsRes.FinanceModule.BusinessServiceMapping"),new TypeReference<List<BusinessService>>(){});
				if(LOGGER.isInfoEnabled())
					LOGGER.info("List of business services ::: "+list);
			}			
		} catch (Exception e) {
			voucherIntegrationLogTO.setStatus("FAILED");
			voucherIntegrationLogTO.setDescription("Error while parsing mdms data.Check the business/account head mapping json file.");
			throw new VoucherCustomException("ERROR","Error while parsing mdms data","check the business/account head mapping json file.");
		}
		return list;
	}
	
	@Override
	public List<TaxHeadMaster> getTaxHeadMasters(String tenantId,String code) throws VoucherCustomException {
		ObjectMapper mapper = new ObjectMapper();
		FilterRequest filter = new FilterRequest();
		filter.setBillingservicecode(code);
		Object ptMdmsData = this.getPTMdmsData(tenantId, "FinanceModule", "TaxHeadMasterGlCodeMapping", filter);
		List<TaxHeadMaster> list = new ArrayList<>();;
		try {
			if(ptMdmsData != null){
				list = mapper.convertValue(JsonPath.read(ptMdmsData, "$.MdmsRes.FinanceModule.TaxHeadMasterGlCodeMapping"),new TypeReference<List<TaxHeadMaster>>(){});
				if(LOGGER.isInfoEnabled())
					LOGGER.info("List of TaxHeadMaster data : "+list);
			}			
		} catch (Exception e) {
			voucherIntegrationLogTO.setStatus("FAILED");
			voucherIntegrationLogTO.setDescription("Error while parsing mdms data.Check the business/account head mapping json file.");
			throw new VoucherCustomException("ERROR","Error while parsing mdms data","check the business/account head mapping json file.");
		}
		return list;
	}
	
	public Object getPTMdmsData(String tenantId, String moduleName, String masterName, FilterRequest filter) throws VoucherCustomException{
		String mdmsUrl = manager.getHostUrl() + manager.getMdmsSearchUrl();
		String authToken = tokenService.generateAdminToken(tenantId); 
        requestInfo.setAuthToken(authToken);
        masterDetails.setName(masterName);
        //Apply filter in the request
        if(null != filter){
            if(null != filter.getBillingservicecode())
                masterDetails.setFilter("[?(@.billingservicecode=='" + filter.getBillingservicecode() + "')]");
            
            if(null != filter.getTaxhead())
                masterDetails.setFilter("[?(@.taxhead=='" + filter.getTaxhead() + "')]");
            
            if(null != filter.getCode())
                masterDetails.setFilter("[?(@.code=='" + filter.getCode() + "')]");
            
            if(null != filter.getCodes()) {
                List<String> codes = filter.getCodes().parallelStream()
                        .map(obj -> {
                            return "'"+obj+"'";
                        }).collect(Collectors.toList());
                masterDetails.setFilter("[?(@.code IN " + codes + ")]");
            }
        }
        moduleDetail.setMasterDetails(Arrays.asList(masterDetails));
        moduleDetail.setModuleName(moduleName);
        mdmscriteria.setTenantId(tenantId);
        mdmscriteria.setModuleDetails(Arrays.asList(moduleDetail));
        mdmsrequest.setRequestInfo(requestInfo);
        mdmsrequest.setMdmsCriteria(mdmscriteria);
        try {
        	if(LOGGER.isInfoEnabled())
        		LOGGER.info("call : "+mdmsUrl);
            Map postForObject = restTemplate.postForObject(mdmsUrl, mdmsrequest, Map.class);
            return postForObject;
        } catch (Exception e) {
        	voucherIntegrationLogTO.setStatus("FAILED");
			voucherIntegrationLogTO.setDescription("Error occured while calling the URL : "+mdmsUrl);
			throw new VoucherCustomException("Error Occured While calling the URL : "+mdmsUrl);
        }
	}
}
