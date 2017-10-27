package org.egov.inv.web.contract;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.egov.inv.domain.model.Store;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public  class StoreRequest {
    @NotNull
    @JsonProperty("requestInfo")
    public RequestInfo requestInfo = new RequestInfo();
    
    @NotNull
    @JsonProperty("stores")
    public List<Store> stores = new ArrayList<Store>();

}
