package org.egov.demand.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jayway.jsonpath.DocumentContext;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.repository.ServiceRequestRepository;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.egov.demand.util.Constants.INVALID_TENANT_ID_MDMS_KEY;
import static org.egov.demand.util.Constants.INVALID_TENANT_ID_MDMS_MSG;

@Component
@Slf4j
public class Util {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;


    public MdmsCriteriaReq prepareMdMsRequest(String tenantId,String moduleName, List<String> names, String filter, RequestInfo requestInfo) {

		List<MasterDetail> masterDetails = new ArrayList<>();
		names.forEach(name -> {

			if (name.equalsIgnoreCase(Constants.TAXPERIOD_MASTERNAME))
				masterDetails.add(MasterDetail.builder().name(name).build());
			else
				masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});

        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName(moduleName).masterDetails(masterDetails).build();
        List<ModuleDetail> moduleDetails = new ArrayList<>();
        moduleDetails.add(moduleDetail);
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
        return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
    }

    public DocumentContext getAttributeValues(MdmsCriteriaReq mdmsReq){
        StringBuilder uri = new StringBuilder(properties.getMdmsHost()).append(properties.getMdmsEndpoint());

        try {
            return serviceRequestRepository.fetchResult(uri.toString(), mdmsReq);
        } catch (Exception e) {
            log.error("Error while fetvhing MDMS data",e);
            throw new CustomException(INVALID_TENANT_ID_MDMS_KEY, INVALID_TENANT_ID_MDMS_MSG);
        }
    }

    public String getStringVal(Set<String> set){
        StringBuilder builder =new StringBuilder();
        int i = 0;
        for(String val : set){
            builder.append(val);
            i++;
            if(i!=set.size())
                builder.append(",");
        }
        return builder.toString();
    }



}
