package org.egov.user.security.oauth2.custom;

import static org.springframework.util.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.SecureUser;
import org.egov.user.domain.model.User;
import org.egov.user.domain.service.UserService;
import org.egov.user.web.contract.Otp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

	/**
	 * TO-Do:Need to remove this and provide authentication for web, based on
	 * authentication_code.
	 */

	private UserService userService;

	@Value("${citizen.login.password.otp.enabled}")
	private boolean citizenLoginPasswordOtpEnabled;

	@Value("${employee.login.password.otp.enabled}")
	private boolean employeeLoginPasswordOtpEnabled;

	private static String tenantId;

	public CustomAuthenticationProvider(UserService userService) {
		this.userService = userService;
	}

	public static String getTenantId() {
		return CustomAuthenticationProvider.tenantId;
	}

	@Override
	public Authentication authenticate(Authentication authentication) {

		String userName = authentication.getName();
		String password = authentication.getCredentials().toString();
		CustomAuthenticationProvider.tenantId = getTenantId(authentication);
		User user;
		/*
		 * if (userName.contains("@") && userName.contains(".")) { user =
		 * userService.getUserByEmailId(userName, tenantId); } else { user =
		 * userService.getUserByUsername(userName, tenantId); }
		 */

		user = userService.getUserByUsername(userName, tenantId);
		if (user == null) {
			String tenant = tenantId;
			if (tenant.contains("."))
				tenant = tenant.split("\\.")[0];
			user = userService.getUserByUsernameAndTenantId(userName, tenant);
		}

		if (user == null)
			throw new OAuth2Exception("Invalid login credentials");

		if (user.getActive() == null || !user.getActive()) {
			throw new OAuth2Exception("Please activate your account");
		}

		System.out.println("tenantId in authenticate------->" + user.getTenantId());

		boolean isCitizen = false;
		if (user.getType() != null && user.getType().toString().equals("CITIZEN"))
			isCitizen = true;

		Boolean isPasswordMatch;
		if (isCitizen) {
			isPasswordMatch = isPasswordMatch(citizenLoginPasswordOtpEnabled, password, user, authentication);
		} else {
			isPasswordMatch = isPasswordMatch(employeeLoginPasswordOtpEnabled, password, user, authentication);
		}

		if (isPasswordMatch) {

			/**
			 * We assume that there will be only one type. If it is multiple
			 * then we have change below code Separate by comma or other and
			 * iterate
			 */
			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			grantedAuths.add(new SimpleGrantedAuthority("ROLE_" + user.getType()));
			final SecureUser secureUser = new SecureUser(getUser(user));
			System.out.println("tenantId in secureUser------->" + secureUser.getTenantId());
			return new UsernamePasswordAuthenticationToken(secureUser, password, grantedAuths);
		} else {
			throw new OAuth2Exception("Invalid login credentials");
		}
	}

	private boolean isPasswordMatch(Boolean isOtpBased, String password, User user, Authentication authentication) {
		BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
		final LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getDetails();
		String isCallInternal = details.get("isInternal");
		if (isOtpBased) {
			if(null != isCallInternal && isCallInternal.equals("true")) {
				log.info("Skipping otp validation during login.........");
				return true;
			}
			Otp otp = Otp.builder().otp(password).identity(user.getUsername()).tenantId(tenantId).build();
			try {
				return userService.validateOtp(otp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new OAuth2Exception(JsonPath.read(e.getMessage(), "$.error.message"));
			}
		} else {
			return bcrypt.matches(password, user.getPassword());
		}
	}

	@SuppressWarnings("unchecked")
	private String getTenantId(Authentication authentication) {
		final LinkedHashMap<String, String> details = (LinkedHashMap<String, String>) authentication.getDetails();

		System.out.println("details------->" + details);
		System.out.println("tenantId in CustomAuthenticationProvider------->" + details.get("tenantId"));

		final String tenantId = details.get("tenantId");
		if (isEmpty(tenantId)) {
			throw new OAuth2Exception("TenantId is mandatory");
		}
		return tenantId;
	}

	private org.egov.user.web.contract.auth.User getUser(User user) {
		return org.egov.user.web.contract.auth.User.builder().id(user.getId()).userName(user.getUsername())
				.name(user.getName()).mobileNumber(user.getMobileNumber()).emailId(user.getEmailId())
				.locale(user.getLocale()).active(user.getActive()).type(user.getType().name())
				.roles(toAuthRole(user.getRoles())).tenantId(user.getTenantId()).build();
	}

	private List<org.egov.user.web.contract.auth.Role> toAuthRole(List<org.egov.user.domain.model.Role> domainRoles) {
		if (domainRoles == null)
			return new ArrayList<>();
		return domainRoles.stream().map(org.egov.user.web.contract.auth.Role::new).collect(Collectors.toList());
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
