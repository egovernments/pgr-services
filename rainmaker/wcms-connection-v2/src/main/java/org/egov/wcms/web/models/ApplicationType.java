package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * description
 */
@ApiModel(description = "description")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationType   {
        @JsonProperty("tenantId")
        private String tenantId = null;

        @JsonProperty("code")
        private String code = null;

        @JsonProperty("name")
        private String name = null;

        @JsonProperty("description")
        private String description = null;

        @JsonProperty("documentType")
        @Valid
        private List<String> documentType = null;


        public ApplicationType addDocumentTypeItem(String documentTypeItem) {
            if (this.documentType == null) {
            this.documentType = new ArrayList<>();
            }
        this.documentType.add(documentTypeItem);
        return this;
        }

}

