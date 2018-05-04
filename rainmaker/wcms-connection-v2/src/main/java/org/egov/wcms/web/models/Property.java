package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Property
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Property   {
        @JsonProperty("propertyId")
        private String propertyId = null;

        @JsonProperty("oldPropertyId")
        private BigDecimal oldPropertyId = null;

        @JsonProperty("address")
        private String address = null;

        @JsonProperty("nameOfApplicant")
        private String nameOfApplicant = null;

        @JsonProperty("mobileNumber")
        private String mobileNumber = null;

        @JsonProperty("email")
        private String email = null;

        @JsonProperty("adharNumber")
        private String adharNumber = null;

        @JsonProperty("locality")
        private String locality = null;


}

