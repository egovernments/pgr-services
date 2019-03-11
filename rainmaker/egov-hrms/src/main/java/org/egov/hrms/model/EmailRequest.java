package org.egov.hrms.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class EmailRequest {
	
    private String email;
    private String subject;
    private String body;
    
    @JsonProperty("isHTML")
    private boolean isHTML;
}
