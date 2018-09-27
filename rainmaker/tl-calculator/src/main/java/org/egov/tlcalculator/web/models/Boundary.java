package org.egov.tlcalculator.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.egov.tlcalculator.web.models.Boundary;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * Boundary
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-27T14:56:03.454+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Boundary   {
        @JsonProperty("code")
        @NotNull
private String code = null;

        @JsonProperty("name")
        @NotNull
private String name = null;

        @JsonProperty("label")
        
private String label = null;

        @JsonProperty("latitude")
        
private String latitude = null;

        @JsonProperty("longitude")
        
private String longitude = null;

        @JsonProperty("children")
        @Valid
        private List<Boundary> children = null;

        @JsonProperty("materializedPath")
        
private String materializedPath = null;


}

