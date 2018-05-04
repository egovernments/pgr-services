package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * Represents meter hardware used for water connection
 */
@ApiModel(description = "Represents meter hardware used for water connection")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Meter   {
        @JsonProperty("id")
        private String id = null;

        @JsonProperty("connectionNumber")
        private String connectionNumber = null;

              /**
   * Holds the owner details of the Meter
   */
  public enum MeterOwnerEnum {
    ULB("ULB"),
    
    CITIZEN("Citizen");

    private String value;

    MeterOwnerEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static MeterOwnerEnum fromValue(String text) {
      for (MeterOwnerEnum b : MeterOwnerEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

        @JsonProperty("meterOwner")
        private MeterOwnerEnum meterOwner = null;

              /**
   * Holds the make details of the Meter
   */
  public enum MeterModelEnum {
    RFID("RFID"),
    
    GENERAL("General");

    private String value;

    MeterModelEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static MeterModelEnum fromValue(String text) {
      for (MeterModelEnum b : MeterModelEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

        @JsonProperty("meterModel")
        private MeterModelEnum meterModel = null;

        @JsonProperty("meterCost")
        private Long meterCost = null;

        @JsonProperty("meterSlNo")
        private Long meterSlNo = null;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails = null;

        @JsonProperty("tenantId")
        private String tenantId = null;


}

