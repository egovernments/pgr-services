package org.egov.demand.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bill
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bill   {
        @JsonProperty("id")
        private String id;

        @JsonProperty("mobileNumber")
        private String mobileNumber;

        @JsonProperty("payerName")
        private String payerName;

        @JsonProperty("payerAddress")
        private String payerAddress;

        @JsonProperty("payerEmail")
        private String payerEmail;

        @JsonProperty("isActive")
        private Boolean isActive;

        @JsonProperty("isCancelled")
        private Boolean isCancelled;

        @JsonProperty("additionalDetails")
        private Object additionalDetails;

        @JsonProperty("collectionMap")
        @Valid
        private Map<String, BigDecimal> collectionMap;

        @JsonProperty("billDetails")
        @Valid
        private List<BillDetail> billDetails;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;


        public Bill putCollectionMapItem(String key, BigDecimal collectionMapItem) {
            if (this.collectionMap == null) {
            this.collectionMap = new HashMap<>();
            }
        this.collectionMap.put(key, collectionMapItem);
        return this;
        }

        public Bill addBillDetailsItem(BillDetail billDetailsItem) {
            if (this.billDetails == null) {
            this.billDetails = new ArrayList<>();
            }
        this.billDetails.add(billDetailsItem);
        return this;
        }

}

