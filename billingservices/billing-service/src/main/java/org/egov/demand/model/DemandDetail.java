package org.egov.demand.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A object holds a demand and collection values for a tax head and period.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemandDetail   {
	
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("demandId")
        private String demandId;

        @JsonProperty("taxHeadMasterCode")
        private String taxHeadMasterCode;

        @JsonProperty("taxAmount")
        private BigDecimal taxAmount;

        @Default
        @JsonProperty("collectionAmount")
        private BigDecimal collectionAmount = BigDecimal.ZERO;

        @JsonProperty("additionalDetails")
        private Object additionalDetails;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;

        @JsonProperty("tenantId")
        private String tenantId;


}

