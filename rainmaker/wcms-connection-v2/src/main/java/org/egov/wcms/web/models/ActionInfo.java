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
 * Capture the details of action on service request.
 */
@ApiModel(description = "Capture the details of action on service request.")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionInfo   {
        @JsonProperty("tenantId")
        private String tenantId = null;

        @JsonProperty("by")
        private String by = null;

        @JsonProperty("isInternal")
        private String isInternal = null;

        @JsonProperty("when")
        private Long when = null;

        @JsonProperty("businessKey")
        private String businessKey = null;

        @JsonProperty("action")
        private String action = null;

        @JsonProperty("status")
        private String status = null;

        @JsonProperty("assignee")
        private String assignee = null;

        @JsonProperty("media")
        @Valid
        private List<String> media = null;

        @JsonProperty("comment")
        private String comment = null;


        public ActionInfo addMediaItem(String mediaItem) {
            if (this.media == null) {
            this.media = new ArrayList<>();
            }
        this.media.add(mediaItem);
        return this;
        }

}

