package org.egov.receipt.consumer.model;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Component
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MdmsCriteriaReq {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;
    @JsonProperty("MdmsCriteria")
    private MdmsCriteria mdmsCriteria;
}
