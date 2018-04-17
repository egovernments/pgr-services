package org.egov.user.web.contract;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.user.domain.model.Address;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.enums.AddressType;
import org.egov.user.domain.model.enums.BloodGroup;
import org.egov.user.domain.model.enums.Gender;
import org.egov.user.domain.model.enums.UserType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserRequest {

	private Long id;
	private String userName;
	private String salutation;
	private String name;
	private String gender;
	private String mobileNumber;
	private String emailId;
	private String altContactNumber;
	private String pan;
	private String aadhaarNumber;
	private String permanentAddress;
	private String permanentCity;
	private String permanentPinCode;
	private String correspondenceAddress;
	private String correspondenceCity;
	private String correspondencePinCode;
	private Boolean active;
	private String locale;
	private UserType type;
	private Boolean accountLocked;
	private String fatherOrHusbandName;
	private String signature;
	private String bloodGroup;
	private String photo;
	private String identificationMark;
	private Long createdBy;
	private String password;
	private String otpReference;
	private Long lastModifiedBy;
	private String tenantId;
	private List<RoleRequest> roles;

	@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
	private Date createdDate;
	@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
	private Date lastModifiedDate;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private Date dob;
	@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
	private Date pwdExpiryDate;

	public UserRequest(User user) {

		this.id = user.getId();
		this.userName = user.getUsername();
		this.salutation = user.getSalutation();
		this.name = user.getName();
		this.gender = user.getGender() != null ? user.getGender().toString() : null;
		this.mobileNumber = user.getMobileNumber();
		this.emailId = user.getEmailId();
		this.altContactNumber = user.getAltContactNumber();
		this.pan = user.getPan();
		this.aadhaarNumber = user.getAadhaarNumber();
		this.active = user.getActive();
		this.dob = user.getDob();
		this.pwdExpiryDate = user.getPasswordExpiryDate();
		this.locale = user.getLocale();
		this.type = user.getType();
		this.accountLocked = user.getAccountLocked();
		this.signature = user.getSignature();
		this.bloodGroup = user.getBloodGroup() != null ? user.getBloodGroup().getValue() : null;
		this.photo = user.getPhoto();
		this.identificationMark = user.getIdentificationMark();
		this.createdBy = user.getCreatedBy();
		this.createdDate = user.getCreatedDate();
		this.lastModifiedBy = user.getLastModifiedBy();
		this.lastModifiedDate = user.getLastModifiedDate();
		this.tenantId = user.getTenantId();
		this.roles = convertDomainRoleToContract(user.getRoles());
		this.fatherOrHusbandName = user.getGuardian();
		mapPermanentAddress(user);
		mapCorrespondenceAddress(user);
	}

	private void mapCorrespondenceAddress(User user) {
		if (user.getCorrespondenceAddress() != null) {
			this.correspondenceAddress = user.getCorrespondenceAddress().getAddress();
			this.correspondenceCity = user.getCorrespondenceAddress().getCity();
			this.correspondencePinCode = user.getCorrespondenceAddress().getPinCode();
		}
	}

	private void mapPermanentAddress(User user) {
		if (user.getPermanentAddress() != null) {
			this.permanentAddress = user.getPermanentAddress().getAddress();
			this.permanentCity = user.getPermanentAddress().getCity();
			this.permanentPinCode = user.getPermanentAddress().getPinCode();
		}
	}

	private List<RoleRequest> convertDomainRoleToContract(List<Role> domainRoles) {
		if (domainRoles == null)
			return new ArrayList<>();
		return domainRoles.stream().map(RoleRequest::new).collect(Collectors.toList());
	}

	@JsonIgnore
	public User toDomain(Long loggedInUserId, boolean isCreate) {
		BloodGroup bloodGroup = null;
		try {
			if (this.bloodGroup != null)
				bloodGroup = BloodGroup.valueOf(this.bloodGroup.toUpperCase());
		} catch (Exception e) {
			for (BloodGroup bloodgroup : BloodGroup.values()) {
				if (bloodgroup.getValue().equals(this.bloodGroup))
					bloodGroup = bloodgroup;
			}
		}
		return User.builder().id(this.id).name(this.name).username(this.userName).salutation(this.salutation)
				.mobileNumber(this.mobileNumber).emailId(this.emailId).altContactNumber(this.altContactNumber)
				.pan(this.pan).aadhaarNumber(this.aadhaarNumber).active(isActive(isCreate)).dob(this.dob)
				.passwordExpiryDate(this.pwdExpiryDate).locale(this.locale).type(this.type)
				.accountLocked(isAccountLocked(isCreate)).signature(this.signature).photo(this.photo)
				.identificationMark(this.identificationMark)
				.gender(this.gender != null ? Gender.valueOf(this.gender.toUpperCase()) : null).bloodGroup(bloodGroup)
				.lastModifiedDate(new Date()).createdDate(new Date()).otpReference(this.otpReference)
				.tenantId(this.tenantId).password(this.password).roles(toDomainRoles()).loggedInUserId(loggedInUserId)
				.permanentAddress(toDomainPermanentAddress()).correspondenceAddress(toDomainCorrespondenceAddress())
				.guardian(fatherOrHusbandName).build();
	}

	private Boolean isActive(boolean isCreate) {
		if (this.active == null && isCreate) {
			return false;
		}
		return this.active;
	}

	private Boolean isAccountLocked(boolean isCreate) {
		if (this.accountLocked == null && isCreate) {
			return false;
		}
		return this.accountLocked;
	}

	private Address toDomainPermanentAddress() {
		return Address.builder().type(AddressType.PERMANENT).city(permanentCity).pinCode(permanentPinCode)
				.address(permanentAddress).build();
	}

	private Address toDomainCorrespondenceAddress() {
		return Address.builder().type(AddressType.CORRESPONDENCE).city(correspondenceCity)
				.pinCode(correspondencePinCode).address(correspondenceAddress).build();
	}

	private List<Role> toDomainRoles() {
		return this.roles != null
				? this.roles.stream().map(RoleRequest::toDomain).distinct().collect(Collectors.toList()) : null;
	}
}
