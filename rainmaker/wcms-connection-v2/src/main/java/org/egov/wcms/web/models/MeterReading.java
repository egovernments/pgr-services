package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.egov.wcms.web.models.AuditDetails;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Captures periodical meter reading of a connection
 */
@ApiModel(description = "Captures periodical meter reading of a connection")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeterReading   {
        @JsonProperty("id")
        private String id = null;

        @JsonProperty("connectionNumber")
        private String connectionNumber = null;

        @JsonProperty("readingDate")
        private Long readingDate = null;

        @JsonProperty("reading")
        private Double reading = null;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails = null;

        @JsonProperty("tenantId")
        private String tenantId = null;

        @JsonProperty("gapCode")
        private String gapCode = null;

        @JsonProperty("consumption")
        private BigDecimal consumption = null;

        @JsonProperty("consumptionAdjusted")
        private BigDecimal consumptionAdjusted = null;

        @JsonProperty("numberOfDays")
        private BigDecimal numberOfDays = null;

        @JsonProperty("resetFlag")
        private Boolean resetFlag = null;


}

