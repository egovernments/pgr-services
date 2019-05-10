package org.egov.receipt.consumer.model;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InstrumentAccountCodeReq {
	@NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("requestInfo")
    private RequestInfo requestInfo;
    
    private InstrumentAccountCodeSearchContract instrumentAccountCodeSearchContract;
}
