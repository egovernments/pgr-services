package org.egov.domain.model;

import static org.springframework.util.StringUtils.isEmpty;

import org.egov.domain.exception.InvalidOtpRequestException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@NoArgsConstructor
public class OtpRequest {
	@Setter
    private String mobileNumber;
    private String tenantId;
    private OtpRequestType type;

    public void validate() {
        if(isTenantIdAbsent()
				|| isMobileNumberAbsent()
				|| isInvalidType()) {
            throw new InvalidOtpRequestException(this);
        }
    }

/*	public boolean isMobileNumberNumeric() {
		// TODO Auto-generated method stub
		if(!(type!=null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString())))
		return !StringUtils.isNumeric(mobileNumber);
		return false;
	}

	public boolean isMobileNumberValidLength() {
		// TODO Auto-generated method stub
		if(!(type!=null && type.toString().equalsIgnoreCase(OtpRequestType.PASSWORD_RESET.toString())))
		return !(mobileNumber != null && mobileNumber.matches("^[0-9]{10,13}$"));
		return false;
	}*/
    
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
