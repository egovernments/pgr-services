package org.egov.domain.model;

import static org.springframework.util.StringUtils.isEmpty;

import org.egov.domain.exception.InvalidOtpRequestException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class OtpRequest {
	@Setter
    private String mobileNumber;
	@Setter
    private String tenantId;
    private OtpRequestType type;

    public void validate() {
        if(isTenantIdAbsent()
				|| isMobileNumberAbsent()
				|| isInvalidType()) {
            throw new InvalidOtpRequestException(this);
        }
    }

	public boolean isRegistrationRequestType() {
    	return OtpRequestType.REGISTER.equals(getType());
	}
	
	public boolean isLoginRequestType() {
    	return OtpRequestType.LOGIN.equals(getType());
	}

	public boolean isInvalidType() {
    	return isEmpty(type);
	}

	public boolean isTenantIdAbsent() {
        return isEmpty(tenantId);
    }

    public boolean isMobileNumberAbsent() {
        return isEmpty(mobileNumber);
    }
}
