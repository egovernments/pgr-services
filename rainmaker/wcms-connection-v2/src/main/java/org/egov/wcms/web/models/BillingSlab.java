package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.egov.wcms.web.models.BillingType;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Billing type master defines types of type of billing for a connection(eg. Plot based, Tab based, Fixed, Other)
 */
@ApiModel(description = "Billing type master defines types of type of billing for a connection(eg. Plot based, Tab based, Fixed, Other)")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingSlab   {
        @JsonProperty("id")
        private String id = null;

        @JsonProperty("billingType")
        private BillingType billingType = null;

        @JsonProperty("from")
        private String from = null;

        @JsonProperty("to")
        private String to = null;

        @JsonProperty("unitRate")
        private Double unitRate = null;

        @JsonProperty("uom")
        private String uom = null;

        @JsonProperty("monthlyAmount")
        private Double monthlyAmount = null;

        @JsonProperty("minimunAmount")
        private Double minimunAmount = null;

        @JsonProperty("fromDate")
        private Long fromDate = null;

        @JsonProperty("toDate")
        private Long toDate = null;

        @JsonProperty("isActive")
        private Boolean isActive = null;

        @JsonProperty("tenantId")
        private String tenantId = null;


}

