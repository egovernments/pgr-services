package org.egov.pgr.model;

import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


/**
 * Instance of Service request raised for a particular service. As per extension propsed in the Service definition \&quot;attributes\&quot; carry the input values requried by metadata definition in the structure as described by the corresponding schema.  * Any one of &#39;address&#39; or &#39;(lat and lang)&#39; or &#39;addressid&#39; is mandatory 
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-03-23T08:00:37.661Z")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service   {
  @JsonProperty("tenantId")
  private String tenantId = null;

  @JsonProperty("serviceCode")
  private String serviceCode = null;

  @JsonProperty("serviceRequestId")
  private String serviceRequestId = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("lat")
  private Double lat = null;

  @JsonProperty("long")
  private Double _long = null;

  @JsonProperty("address")
  private String address = null;

  @JsonProperty("addressId")
  private String addressId = null;

  @JsonProperty("email")
  @Email
  private String email = null;

  @JsonProperty("deviceId")
  private String deviceId = null;

  @JsonProperty("accountId")
  private String accountId = null;

  @JsonProperty("firstName")
  private String firstName = null;

  @JsonProperty("lastName")
  private String lastName = null;

  @JsonProperty("phone")
  @NotEmpty
  @Pattern(regexp="(^$|[0-9]{10})")
  private String phone = null;

  @JsonProperty("attributes")
  private Object attributes = null;

  /**
   * The current status of the service request.
   */
  public enum StatusEnum {
	  
	OPEN("open"),
	
	ASSIGNED("assigned"),
	
    NEW("New"),
    
    INPROGRESS("InProgress"),
    
    CLOSED("Closed"),
    
    CANCELLED("Cancelled"),
    
    REJECTED("Rejected"),
    
    RESOLVED("Resolved");    

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equalsIgnoreCase(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("status")
  private StatusEnum status = null;

  /**
   * source of the complaint - Text, Mobile app, Phone, CSC, WhatsApp
   */
  public enum SourceEnum {
    SMS("sms"),
    
    EMAIL("email"),
    
    IVR("ivr"),
    
    MOBILEAPP("mobileapp"),
    
    WHATSAPP("whatsapp"),
    
    CSC("csc"),
    
    WEB("web");

    private String value;

    SourceEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SourceEnum fromValue(String text) {
      for (SourceEnum b : SourceEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("source")
  private SourceEnum source = null;

  @JsonProperty("expectedTime")
  private Long expectedTime = null;

  @JsonProperty("feedback")
  private String feedback = null;

  @JsonProperty("rating")
  private String rating = null;

  @JsonProperty("auditDetails")
  private AuditDetails auditDetails = null;
  
  @JsonProperty("landmark")
  private String landmark;
  
  public void setLandmark(String landmark) {
	  this.landmark = landmark;
  }
  
  public String getLandmark() {
	  return landmark;
  }

  public Service tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * The unique identifier for Service - this is equivalent to jurisdiction_id in Open311. As the platform intends to be multi tenanted - this is always required
   * @return tenantId
  **/
  @NotNull

@Size(min=2,max=50) 
  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Service serviceCode(String serviceCode) {
    this.serviceCode = serviceCode;
    return this;
  }

  /**
   * Code of the service being requested (as per the service definition)
   * @return serviceCode
  **/
  @NotNull

@Size(min=2,max=64) 
  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public Service serviceRequestId(String serviceRequestId) {
    this.serviceRequestId = serviceRequestId;
    return this;
  }

  /**
   * The server generated unique ID of the service request.
   * @return serviceRequestId
  **/

@Size(min=2,max=64) 
  public String getServiceRequestId() {
    return serviceRequestId;
  }

  public void setServiceRequestId(String serviceRequestId) {
    this.serviceRequestId = serviceRequestId;
  }

  public Service description(String description) {
    this.description = description;
    return this;
  }

  /**
   * A full description of the request provided by the user
   * @return description
  **/
  @NotNull

@Size(min=2,max=500) 
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Service lat(Double lat) {
    this.lat = lat;
    return this;
  }

  /**
   * Latitude of the location.
   * @return lat
  **/
  public Double getLat() {
    return lat;
  }

  public void setLat(Double lat) {
    this.lat = lat;
  }

  public Service _long(Double _long) {
    this._long = _long;
    return this;
  }

  /**
   * Longitude of location.
   * @return _long
  **/
  public Double getLong() {
    return _long;
  }

  public void setLong(Double _long) {
    this._long = _long;
  }

  public Service address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Human readable address or description of location.
   * @return address
  **/
@Size(min=2,max=256) 
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Service addressId(String addressId) {
    this.addressId = addressId;
    return this;
  }

  /**
   * The internal address ID used by a tenant master address repository or other addressing system - for eGov it can be boundary id in the boundary service
   * @return addressId
  **/
  public String getAddressId() {
    return addressId;
  }

  public void setAddressId(String addressId) {
    this.addressId = addressId;
  }

  public Service email(String email) {
    this.email = email;
    return this;
  }

  /**
   * The email address of the person submitting the request.
   * @return email
  **/
@Size(min=5,max=50) 
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Service deviceId(String deviceId) {
    this.deviceId = deviceId;
    return this;
  }

  /**
   * The unique device ID of the device submitting the request.
   * @return deviceId
  **/

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public Service accountId(String accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   * userid of the user requesting the srervice - in our case it may be same as phone as we are using mobile number as the userid
   * @return accountId
  **/
@Size(min=2,max=64) 
  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public Service firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  /**
   * The given name of the person submitting the request.
   * @return firstName
  **/
@Size(min=2,max=32) 
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public Service lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  /**
   * The family name of the person submitting the request.
   * @return lastName
  **/
@Size(min=2,max=32) 
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Service phone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * The phone number of the person submitting the request.
   * @return phone
  **/
  @NotNull

@Size(max=12) 
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Service attributes(Object attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * This is the json object that will carry the actual input (whereever the metadata requries input). Structure should be same as the schema definition provided in the metadata of the service (schema compliance check to be performed at client/server)
   * @return attributes
  **/

  public Object getAttributes() {
    return attributes;
  }

  public void setAttributes(Object attributes) {
    this.attributes = attributes;
  }

  public Service status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * The current status of the service request.
   * @return status
  **/
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Service source(SourceEnum source) {
    this.source = source;
    return this;
  }

  /**
   * source of the complaint - Text, Mobile app, Phone, CSC, WhatsApp
   * @return source
  **/
  public SourceEnum getSource() {
    return source;
  }

  public void setSource(SourceEnum source) {
    this.source = source;
  }

  public Service expectedTime(Long expectedTime) {
    this.expectedTime = expectedTime;
    return this;
  }

  /**
   * epoch of the time object is last modified.
   * @return expectedTime
  **/
  public Long getExpectedTime() {
    return expectedTime;
  }

  public void setExpectedTime(Long expectedTime) {
    this.expectedTime = expectedTime;
  }

  public Service feedback(String feedback) {
    this.feedback = feedback;
    return this;
  }

  /**
   * FeedBack of the service by citizen.
   * @return feedback
  **/
@Size(min=2,max=64) 
  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public Service rating(String rating) {
    this.rating = rating;
    return this;
  }

  /**
   * Rating about service from 1 to 5.
   * @return rating
  **/
  @Size(min=1,max=5) 
  public String getRating() {
    return rating;
  }

  public void setRating(String rating) {
    this.rating = rating;
  }

  public Service auditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
    return this;
  }

  /**
   * Get auditDetails
   * @return auditDetails
  **/
  @Valid

  public AuditDetails getAuditDetails() {
    return auditDetails;
  }

  public void setAuditDetails(AuditDetails auditDetails) {
    this.auditDetails = auditDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Service service = (Service) o;
    return Objects.equals(this.tenantId, service.tenantId) &&
        Objects.equals(this.serviceCode, service.serviceCode) &&
        Objects.equals(this.serviceRequestId, service.serviceRequestId) &&
        Objects.equals(this.description, service.description) &&
        Objects.equals(this.lat, service.lat) &&
        Objects.equals(this._long, service._long) &&
        Objects.equals(this.address, service.address) &&
        Objects.equals(this.addressId, service.addressId) &&
        Objects.equals(this.email, service.email) &&
        Objects.equals(this.deviceId, service.deviceId) &&
        Objects.equals(this.accountId, service.accountId) &&
        Objects.equals(this.firstName, service.firstName) &&
        Objects.equals(this.lastName, service.lastName) &&
        Objects.equals(this.phone, service.phone) &&
        Objects.equals(this.attributes, service.attributes) &&
        Objects.equals(this.status, service.status) &&
        Objects.equals(this.source, service.source) &&
        Objects.equals(this.expectedTime, service.expectedTime) &&
        Objects.equals(this.feedback, service.feedback) &&
        Objects.equals(this.rating, service.rating) &&
        Objects.equals(this.auditDetails, service.auditDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tenantId, serviceCode, serviceRequestId, description, lat, _long, address, addressId, email, deviceId, accountId, firstName, lastName, phone, attributes, status, source, expectedTime, feedback, rating, auditDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Service {\n");
    
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    serviceCode: ").append(toIndentedString(serviceCode)).append("\n");
    sb.append("    serviceRequestId: ").append(toIndentedString(serviceRequestId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    lat: ").append(toIndentedString(lat)).append("\n");
    sb.append("    _long: ").append(toIndentedString(_long)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    addressId: ").append(toIndentedString(addressId)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
    sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
    sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    expectedTime: ").append(toIndentedString(expectedTime)).append("\n");
    sb.append("    feedback: ").append(toIndentedString(feedback)).append("\n");
    sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
    sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

