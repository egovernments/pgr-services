package org.egov.user.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.user.domain.exception.AtleastOneRoleCodeException;
import org.egov.user.domain.exception.DuplicateUserNameException;
import org.egov.user.domain.exception.InvalidUpdatePasswordRequestException;
import org.egov.user.domain.exception.InvalidUserCreateException;
import org.egov.user.domain.exception.OtpValidationPendingException;
import org.egov.user.domain.exception.PasswordMismatchException;
import org.egov.user.domain.exception.UserIdMandatoryException;
import org.egov.user.domain.exception.UserNameNotValidException;
import org.egov.user.domain.exception.UserNotFoundException;
import org.egov.user.domain.exception.UserProfileUpdateDeniedException;
import org.egov.user.domain.model.LoggedInUserUpdatePasswordRequest;
import org.egov.user.domain.model.NonLoggedInUserUpdatePasswordRequest;
import org.egov.user.domain.model.OtpValidationRequest;
import org.egov.user.domain.model.Role;
import org.egov.user.domain.model.User;
import org.egov.user.domain.model.UserSearchCriteria;
import org.egov.user.domain.model.enums.Gender;
import org.egov.user.domain.model.enums.UserType;
import org.egov.user.persistence.repository.FileStoreRepository;
import org.egov.user.persistence.repository.OtpRepository;
import org.egov.user.persistence.repository.UserRepository;
import org.egov.user.web.contract.Otp;
import org.egov.user.web.contract.OtpValidateRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	private static final int DEFAULT_PASSWORD_EXPIRY_IN_DAYS = 90;
	@Mock
	private UserRepository userRepository;

	@Mock
	private OtpRepository otpRepository;
	
	@Mock
	private FileStoreRepository fileRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UserService userService;

	private final List<Long> ID = Arrays.asList(1L, 2L);
	private final String EMAIL = "email@gmail.com";
	private final String USER_NAME = "userName";
	private final String TENANT_ID = "tenantId";
	private final boolean isCitizenLoginOtpBased = false;
	private final boolean isEmployeeLoginOtpBased = false;

	@Before
	public void before() {
		userService = new UserService(userRepository, otpRepository,fileRepository, passwordEncoder, DEFAULT_PASSWORD_EXPIRY_IN_DAYS,
				isCitizenLoginOtpBased,isEmployeeLoginOtpBased);
	}

	@Test
	public void test_should_get_user_by_email() {
		when(userRepository.findByEmailId(EMAIL, TENANT_ID)).thenReturn(getUserObject());

		User actualUser = userService.getUserByEmailId(EMAIL, TENANT_ID);

		assertThat(actualUser.getEmailId()).isEqualTo(EMAIL);
	}

	@Test
	public void test_should_get_user_by_username() {
		when(userRepository.findByUsername(USER_NAME, TENANT_ID)).thenReturn(getUserObject());

		User actualUser = userService.getUserByUsername(USER_NAME, TENANT_ID);

		assertThat(actualUser.getUsername()).isEqualTo(USER_NAME);
	}

	@Test
	public void test_should_search_for_users() {
		UserSearchCriteria userSearch = mock(UserSearchCriteria.class);
		List<org.egov.user.domain.model.User> expectedListOfUsers = mock(List.class);
		when(userRepository.findAll(userSearch)).thenReturn(expectedListOfUsers);

		List<org.egov.user.domain.model.User> actualResult = userService.searchUsers(userSearch);

		assertThat(expectedListOfUsers).isEqualTo(actualResult);
	}

	@Test
	public void test_should_validate_search_critieria() {
		UserSearchCriteria userSearch = mock(UserSearchCriteria.class);
		List<org.egov.user.domain.model.User> expectedListOfUsers = mock(List.class);
		when(userRepository.findAll(userSearch)).thenReturn(expectedListOfUsers);

		userService.searchUsers(userSearch);

		verify(userSearch).validate();
	}

	@Test
	public void test_should_save_a_valid_user() {
		org.egov.user.domain.model.User domainUser = validDomainUser(false);
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		final User expectedEntityUser = User.builder().build();
		when(userRepository.create(domainUser)).thenReturn(expectedEntityUser);

		User returnedUser = userService.createUser(domainUser);

		assertEquals(expectedEntityUser, returnedUser);
	}

	@Test
	public void test_should_set_pre_defined_expiry_on_creating_user() {
		org.egov.user.domain.model.User domainUser = mock(User.class);
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		final User expectedEntityUser = User.builder().build();
		when(userRepository.create(domainUser)).thenReturn(expectedEntityUser);

		userService.createUser(domainUser);

		verify(domainUser).setDefaultPasswordExpiry(DEFAULT_PASSWORD_EXPIRY_IN_DAYS);
	}

	@Test
	public void test_should_create_a_valid_citizen() {
		org.egov.user.domain.model.User domainUser = mock(User.class);
		when((domainUser.getOtpValidationRequest())).thenReturn(getExpectedRequest());
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		final User expectedUser = User.builder().build();
		when(domainUser.getTenantId()).thenReturn("default");
		when(userRepository.create(domainUser)).thenReturn(expectedUser);

		User returnedUser = userService.createCitizen(domainUser);

		assertEquals(expectedUser, returnedUser);
	}

	@Test(expected = UserNameNotValidException.class)
	public void test_should_not_create_citizenWithWrongUserName() {
		userService = new UserService(userRepository, otpRepository,fileRepository, passwordEncoder, DEFAULT_PASSWORD_EXPIRY_IN_DAYS,
				true,false);
		org.egov.user.domain.model.User domainUser = User.builder().username("TestUser").name("Test").active(true)
				.tenantId("default").mobileNumber("123456789").type(UserType.CITIZEN).build();
		userService.createCitizen(domainUser);
	}

	
	@Test
	public void test_should_create_a_valid_citizen_withotp() throws Exception {
		org.egov.user.domain.model.User domainUser = mock(User.class);
		when(domainUser.getTenantId()).thenReturn("default");
		when((domainUser.getOtpValidationRequest())).thenReturn(getExpectedRequest());
		when(otpRepository.validateOtp(buildOtpValidationRequest())).thenReturn(true);
		final User expectedUser = User.builder().build();
		when(userRepository.create(domainUser)).thenReturn(expectedUser);

		User returnedUser = userService.createCitizen(domainUser);

		assertEquals(expectedUser, returnedUser);
	}

	private org.egov.user.web.contract.OtpValidateRequest buildOtpValidationRequest() {
		// TODO Auto-generated method stub
		RequestInfo requestInfo = RequestInfo.builder().action("validate").ts(new Date()).build();
		Otp otp = Otp.builder().build();
		org.egov.user.web.contract.OtpValidateRequest otpValidationRequest = org.egov.user.web.contract.OtpValidateRequest
				.builder().requestInfo(requestInfo).otp(otp).build();
		return otpValidationRequest;
	}

	@Test
	public void test_should_set_pre_defined_expiry_on_creating_citizen() {
		org.egov.user.domain.model.User domainUser = mock(User.class);
		when(domainUser.getTenantId()).thenReturn("default");
		when((domainUser.getOtpValidationRequest())).thenReturn(getExpectedRequest());
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		final User expectedUser = User.builder().build();
		when(userRepository.create(domainUser)).thenReturn(expectedUser);

		userService.createCitizen(domainUser);

		verify(domainUser).setDefaultPasswordExpiry(DEFAULT_PASSWORD_EXPIRY_IN_DAYS);
	}

	@Test
	public void test_should_set_role_to_citizen_when_creating_a_citizen() {
		org.egov.user.domain.model.User domainUser = mock(User.class);
		when(domainUser.getTenantId()).thenReturn("default");
		when(domainUser.getOtpValidationRequest()).thenReturn(getExpectedRequest());
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		final User expectedEntityUser = User.builder().build();
		when(userRepository.create(domainUser)).thenReturn(expectedEntityUser);

		userService.createCitizen(domainUser);

		verify(domainUser).setRoleToCitizen();
	}

	@Test(expected = DuplicateUserNameException.class)
	public void test_should_raise_exception_when_duplicate_user_name_exists() throws Exception {
		org.egov.user.domain.model.User domainUser = validDomainUser(false);
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		when(userRepository.isUserPresent("supandi_rocks", "tenantId")).thenReturn(true);

		userService.createUser(domainUser);
	}

	@Test(expected = OtpValidationPendingException.class)
	public void test_exception_is_raised_when_otp_validation_fails() throws Exception {
		org.egov.user.domain.model.User domainUser = validDomainUser(false);
		domainUser.setOtpValidationMandatory(true);
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(false);

		userService.createUser(domainUser);
	}

	@Test
	public void test_otp_is_not_validated_when_validation_flag_is_false() throws Exception {
		org.egov.user.domain.model.User domainUser = validDomainUser(false);
		when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(false);

		userService.createUser(domainUser);

		verify(otpRepository, never()).isOtpValidationComplete(getExpectedRequest());
	}

	@Test(expected = InvalidUserCreateException.class)
	public void test_should_raise_exception_when_user_is_invalid() throws Exception {
		org.egov.user.domain.model.User domainUser = org.egov.user.domain.model.User.builder().build();

		userService.createUser(domainUser);
		verify(userRepository, never()).create(any(org.egov.user.domain.model.User.class));
	}

	@Test
	public void test_should_update_a_valid_user() {
		User domainUser = validDomainUser(false);
		User user = User.builder().build();
		final User expectedUser = User.builder().build();
		when(userRepository.update(any(org.egov.user.domain.model.User.class))).thenReturn(expectedUser);
		when(userRepository.getUserById(any(Long.class), anyString())).thenReturn(user);
		when(userRepository.isUserPresent(any(String.class), any(Long.class), any(String.class))).thenReturn(false);

		User returnedUser = userService.updateWithoutOtpValidation(1L, domainUser);

		assertEquals(expectedUser, returnedUser);
	}

	@Test(expected = AtleastOneRoleCodeException.class)
	public void test_should_validate_user_on_update() {
		User domainUser = mock(User.class);
		Role role = Role.builder().code("EMPLOYEE").build();
		List<Role> roles = new ArrayList<Role>();
		roles.add(role);
		domainUser.setRoles(roles);
		User user = User.builder().build();
		final User expectedUser = User.builder().build();
		expectedUser.setRoles(roles);
		when(userRepository.update(any(org.egov.user.domain.model.User.class))).thenReturn(expectedUser);
		when(userRepository.getUserById(any(Long.class), anyString())).thenReturn(user);
		when(userRepository.isUserPresent(any(String.class), any(Long.class), any(String.class))).thenReturn(false);
		userService.updateWithoutOtpValidation(1L, domainUser);
		verify(domainUser).validateUserModification();
	}

	@Test(expected = DuplicateUserNameException.class)
	public void test_should_throw_error_when_username_exists_while_updating() throws Exception {
		User domainUser = validDomainUser(false);
		when(userRepository.isUserPresent(any(String.class), any(Long.class), any(String.class))).thenReturn(true);

		userService.updateWithoutOtpValidation(1L, domainUser);
	}

	@Test(expected = UserNotFoundException.class)
	public void test_should_throw_error_when_user_not_exists_while_updating() throws Exception {
		User domainUser = validDomainUser(false);
		when(userRepository.isUserPresent(any(String.class), any(Long.class), any(String.class))).thenReturn(false);
		when(userRepository.getUserById(any(Long.class), anyString())).thenReturn(null);

		userService.updateWithoutOtpValidation(1L, domainUser);
	}

	@Test(expected = UserIdMandatoryException.class)
	public void test_should_throw_exception_on_partial_update_when_id_is_not_present() {
		final User user = User.builder().id(null).build();

		userService.partialUpdate(user);
	}

	@Test
	public void test_should_nullify_fields_that_are_not_allowed_to_be_updated() throws Exception {
		final User user = mock(User.class);
		when(userRepository.update(user)).thenReturn(user);
        when(fileRepository.getUrlByFileStoreId(any(String.class), any(String.class))).thenReturn("fileStoreUrl");
		userService.partialUpdate(user);

		verify(user).nullifySensitiveFields();
	}

	@Test
	public void test_should_partially_update_user() throws Exception {
		final User user = mock(User.class);
		final long userId = 123L;
		when(user.getId()).thenReturn(userId);
		when(userRepository.update(user)).thenReturn(user);
        when(fileRepository.getUrlByFileStoreId(any(String.class), any(String.class))).thenReturn("fileStoreUrl");
		userService.partialUpdate(user);

		verify(userRepository).update(user);
	}

	@Test(expected = UserProfileUpdateDeniedException.class)
	public void test_should_throw_exception_when_logged_in_user_is_different_from_user_being_updated() {
		final User user = mock(User.class);
		when(user.isLoggedInUserDifferentFromUpdatedUser()).thenReturn(true);

		userService.partialUpdate(user);
	}

	@Test
	public void test_should_validate_update_password_request() {
		final LoggedInUserUpdatePasswordRequest updatePasswordRequest = mock(LoggedInUserUpdatePasswordRequest.class);
		User user = mock(User.class);
		when(user.getType()).thenReturn(UserType.CITIZEN);
		when(userRepository.getUserById(any(), anyString())).thenReturn(user);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

		userService.updatePasswordForLoggedInUser(updatePasswordRequest);

		verify(updatePasswordRequest).validate();
	}
	
	@Test(expected = InvalidUpdatePasswordRequestException.class)
	public void test_should_throwexception_incaseofloginotpenabledastrue_forcitizen_update_password_request() {
		userService = new UserService(userRepository, otpRepository,fileRepository, passwordEncoder, DEFAULT_PASSWORD_EXPIRY_IN_DAYS,
				true,isEmployeeLoginOtpBased);
		User user = mock(User.class);
		when(userRepository.getUserById(any(), anyString())).thenReturn(user);
		when(user.getType()).thenReturn(UserType.CITIZEN);
		final LoggedInUserUpdatePasswordRequest updatePasswordRequest = mock(LoggedInUserUpdatePasswordRequest.class);
		userService.updatePasswordForLoggedInUser(updatePasswordRequest);
	}
	
	@Test(expected = InvalidUpdatePasswordRequestException.class)
	public void test_should_throwexception_incaseofloginotpenabledastrue_foremployee_update_password_request() {
		userService = new UserService(userRepository, otpRepository,fileRepository, passwordEncoder, DEFAULT_PASSWORD_EXPIRY_IN_DAYS,
				false,true);
		User user = mock(User.class);
		when(userRepository.getUserById(any(), anyString())).thenReturn(user);
		when(user.getType()).thenReturn(UserType.EMPLOYEE);
		final LoggedInUserUpdatePasswordRequest updatePasswordRequest = mock(LoggedInUserUpdatePasswordRequest.class);
		userService.updatePasswordForLoggedInUser(updatePasswordRequest);
	}

	@Test(expected = UserNotFoundException.class)
	public void test_should_throw_exception_when_attempting_to_update_password_for_a_user_that_does_not_exist() {
		final LoggedInUserUpdatePasswordRequest updatePasswordRequest = mock(LoggedInUserUpdatePasswordRequest.class);
		when(updatePasswordRequest.getUserId()).thenReturn(123L);
		when(updatePasswordRequest.getTenantId()).thenReturn("tenantId");
		when(userRepository.getUserById(123L, "tenantId")).thenReturn(null);

		userService.updatePasswordForLoggedInUser(updatePasswordRequest);
	}

	@Test(expected = PasswordMismatchException.class)
	public void test_should_throw_exception_when_existing_password_does_not_match_on_attempting_to_update_user() {
		final LoggedInUserUpdatePasswordRequest updatePasswordRequest = mock(LoggedInUserUpdatePasswordRequest.class);
		when(updatePasswordRequest.getExistingPassword()).thenReturn("wrongPassword");
		final User user = mock(User.class);
		when(user.getType()).thenReturn(UserType.CITIZEN);
		when(user.getPassword()).thenReturn("existingPasswordEncoded");
		when(user.getPassword()).thenReturn("existingPasswordEncoded");
		when(passwordEncoder.matches("wrongPassword", "existingPasswordEncoded")).thenReturn(false);
		when(userRepository.getUserById(any(), anyString())).thenReturn(user);

		userService.updatePasswordForLoggedInUser(updatePasswordRequest);
	}

	@Test
	public void test_should_update_password_for_logged_in_user() {
		final LoggedInUserUpdatePasswordRequest updatePasswordRequest = mock(LoggedInUserUpdatePasswordRequest.class);
		when(updatePasswordRequest.getExistingPassword()).thenReturn("existingPassword");
		final User domainUser = mock(User.class);
		when(domainUser.getType()).thenReturn(UserType.CITIZEN);
		when(domainUser.getPassword()).thenReturn("existingPasswordEncoded");
		when(passwordEncoder.matches("existingPassword", "existingPasswordEncoded")).thenReturn(true);
		when(userRepository.getUserById(any(), anyString())).thenReturn(domainUser);

		userService.updatePasswordForLoggedInUser(updatePasswordRequest);

		verify(domainUser).updatePassword(updatePasswordRequest.getNewPassword());
		verify(userRepository).update(domainUser);
	}

	@Test
	public void test_should_validate_request_when_updating_password_for_non_logged_in_user() {
		final NonLoggedInUserUpdatePasswordRequest request = mock(NonLoggedInUserUpdatePasswordRequest.class);
		when(otpRepository.isOtpValidationComplete(any())).thenReturn(true);
		final User domainUser = User.builder().type(UserType.SYSTEM).build();
		when(userRepository.findByUsername(anyString(), anyString())).thenReturn(domainUser);

		userService.updatePasswordForNonLoggedInUser(request);

		verify(request).validate();
	}

	@Test(expected = UserNotFoundException.class)
	public void test_should_throw_exception_when_user_does_not_exist_when_updating_password_for_non_logged_in_user() {
		final NonLoggedInUserUpdatePasswordRequest request = mock(NonLoggedInUserUpdatePasswordRequest.class);
		when(otpRepository.isOtpValidationComplete(any())).thenReturn(true);
		when(userRepository.findByUsername(anyString(), anyString())).thenReturn(null);

		userService.updatePasswordForNonLoggedInUser(request);
	}

	@Test
	public void test_should_update_existing_password_for_non_logged_in_user() throws Exception {
		final NonLoggedInUserUpdatePasswordRequest request = mock(NonLoggedInUserUpdatePasswordRequest.class);
		when(request.getNewPassword()).thenReturn("newPassword");
		when(otpRepository.validateOtp(any())).thenReturn(true);
		final User domainUser = mock(User.class);
		when(domainUser.getPassword()).thenReturn("newPassword");
		when(domainUser.getType()).thenReturn(UserType.SYSTEM);
		when(userRepository.findByUsername(anyString(), anyString())).thenReturn(domainUser);

		userService.updatePasswordForNonLoggedInUser(request);

		verify(domainUser).updatePassword("newPassword");
	}

	@SuppressWarnings("unchecked")
	@Test(expected = Exception.class)
	public void test_notshould_update_existing_password_for_non_logged_in_user() throws Exception {
		final NonLoggedInUserUpdatePasswordRequest request = mock(NonLoggedInUserUpdatePasswordRequest.class);
		when(request.getNewPassword()).thenReturn("newPassword");
		when(otpRepository.validateOtp(any())).thenThrow(Exception.class);
		final User domainUser = mock(User.class);
		when(userRepository.findByUsername(anyString(), anyString())).thenReturn(domainUser);

		userService.updatePasswordForNonLoggedInUser(request);

		verify(domainUser).updatePassword("newPassword");
	}

	@SuppressWarnings("unchecked")
	@Test(expected = InvalidUpdatePasswordRequestException.class)
	public void test_notshould_update_password_whenCitizenotpconfigured_istrue() throws Exception {
		userService = new UserService(userRepository, otpRepository,fileRepository, passwordEncoder, DEFAULT_PASSWORD_EXPIRY_IN_DAYS,
				true,false);
		final NonLoggedInUserUpdatePasswordRequest request = mock(NonLoggedInUserUpdatePasswordRequest.class);
		when(request.getNewPassword()).thenReturn("newPassword");
		when(otpRepository.validateOtp(any())).thenThrow(Exception.class);
		final User domainUser = mock(User.class);
		when(domainUser.getType()).thenReturn(UserType.CITIZEN); 
		when(userRepository.findByUsername(anyString(), anyString())).thenReturn(domainUser);
		userService.updatePasswordForNonLoggedInUser(request);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = InvalidUpdatePasswordRequestException.class)
	public void test_notshould_update_password_whenEmployeeotpconfigured_istrue() throws Exception {
		userService = new UserService(userRepository, otpRepository,fileRepository, passwordEncoder, DEFAULT_PASSWORD_EXPIRY_IN_DAYS,
				false,true);
		final NonLoggedInUserUpdatePasswordRequest request = mock(NonLoggedInUserUpdatePasswordRequest.class);
		when(request.getNewPassword()).thenReturn("newPassword");
		when(otpRepository.validateOtp(any())).thenThrow(Exception.class);
		final User domainUser = mock(User.class);
		when(domainUser.getType()).thenReturn(UserType.EMPLOYEE); 
		when(userRepository.findByUsername(anyString(), anyString())).thenReturn(domainUser);
		userService.updatePasswordForNonLoggedInUser(request);
	}
	
	@Test
	public void test_should_create_a_valid_citizen_WithOtp() throws Exception {
		org.egov.user.domain.model.User domainUser = mock(User.class);
		when((domainUser.getOtpValidationRequest())).thenReturn(getExpectedRequest());
		// when(otpRepository.isOtpValidationComplete(getExpectedRequest())).thenReturn(true);
		when(otpRepository.validateOtp((getOtpValidationRequest()))).thenReturn(true);
		final User expectedUser = User.builder().build();
		when(userRepository.create(domainUser)).thenReturn(expectedUser);

		User returnedUser = userService.createCitizen(domainUser);

		assertEquals(expectedUser, returnedUser);
	}
	
	private OtpValidateRequest getOtpValidationRequest() {

		RequestInfo requestInfo = RequestInfo.builder().build();

		Otp otp = Otp.builder().identity("12121212").otp("23456").tenantId("default").build();

		return OtpValidateRequest.builder().requestInfo(requestInfo).otp(otp).build();

	}

	@Test
	public void test_should_persist_changes_on_updating_password_for_non_logged_in_user() {
		final NonLoggedInUserUpdatePasswordRequest request = NonLoggedInUserUpdatePasswordRequest.builder()
				.userName("mobileNumber").tenantId("tenant").otpReference("otpReference").newPassword("newPassword")
				.build();
		final OtpValidationRequest expectedRequest = OtpValidationRequest.builder().otpReference("otpReference")
				.mobileNumber("mobileNumber").tenantId("tenant").build();
		when(otpRepository.isOtpValidationComplete(expectedRequest)).thenReturn(true);
		final User domainUser =User.builder().type(UserType.SYSTEM).build();
		when(userRepository.findByUsername("mobileNumber", "tenant")).thenReturn(domainUser);

		userService.updatePasswordForNonLoggedInUser(request);

		verify(userRepository).update(domainUser);
	}

	private org.egov.user.domain.model.User validDomainUser(boolean otpValidationMandatory) {
		return User.builder().username("supandi_rocks").name("Supandi").gender(Gender.MALE).type(UserType.CITIZEN)
				.active(Boolean.TRUE).mobileNumber("9988776655").tenantId("tenantId").otpReference("12312")
				.password("password").roles(Collections.singletonList(Role.builder().code("roleCode1").build()))
				.accountLocked(false).otpValidationMandatory(otpValidationMandatory).build();
	}

	private OtpValidationRequest getExpectedRequest() {
		return OtpValidationRequest.builder().otpReference("12312").tenantId("tenantId").mobileNumber("9988776655")
				.build();
	}

	private User getUserObject() {
		return User.builder().id(ID.get(0)).emailId(EMAIL).username(USER_NAME).build();
	}
}