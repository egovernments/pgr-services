package org.egov.receipt.consumer.model;

import java.util.List;

import org.springframework.stereotype.Component;
@Component
public class MdmsCriteria {

    private String tenantId;
    
    private List<ModuleDetail> moduleDetails;

    public MdmsCriteria(String tenantId, List<ModuleDetail> moduleDetails) {
        this.tenantId = tenantId;
        this.moduleDetails = moduleDetails;
    }
    
    public MdmsCriteria(){}

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<ModuleDetail> getModuleDetails() {
        return moduleDetails;
    }

    public void setModuleDetails(List<ModuleDetail> moduleDetails) {
        this.moduleDetails = moduleDetails;
    }
    
    
}
