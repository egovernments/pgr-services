package org.egov.user.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@Builder
@ToString
public class Otp {
    private String otp;
    @JsonProperty("UUID")
    private String uuid;
    private String identity;
    private String tenantId;
    @JsonProperty("isValidationSuccessful")
    private boolean validationSuccessful;
}