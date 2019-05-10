package org.egov.receipt.consumer.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown=true)
public @Data class InstrumentAccountCodeResponse {
    private ResponseInfo responseInfo;
    private List<InstrumentAccountCodeContract> instrumentAccountCodes;
}