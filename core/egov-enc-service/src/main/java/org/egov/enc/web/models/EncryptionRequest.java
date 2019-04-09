package org.egov.enc.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EncryptionRequest {

    @JsonProperty("encryptionRequests")
    private List<EncReqObject> encryptionRequests;

}
