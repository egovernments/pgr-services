package org.egov.pgr.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.model.ActionInfo;
import org.egov.pgr.model.Service;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Request object to fetch the report data
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-03-23T08:00:37.661Z")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequest   {
  @JsonProperty("RequestInfo")
  private RequestInfo requestInfo = null;

  @JsonProperty("services")
  @Valid
  private List<Service> services = new ArrayList<Service>();

  @JsonProperty("actionInfo")
  @Valid
  private List<ActionInfo> actionInfo = null;

  public ServiceRequest requestInfo(RequestInfo requestInfo) {
    this.requestInfo = requestInfo;
    return this;
  }

  /**
   * Get requestInfo
   * @return requestInfo
  **/
  @NotNull

  @Valid

  public RequestInfo getRequestInfo() {
    return requestInfo;
  }

  public void setRequestInfo(RequestInfo requestInfo) {
    this.requestInfo = requestInfo;
  }

  public ServiceRequest services(List<Service> services) {
    this.services = services;
    return this;
  }

  public ServiceRequest addServicesItem(Service servicesItem) {
    this.services.add(servicesItem);
    return this;
  }

  /**
   * Get services
   * @return services
  **/
  @NotNull

  @Valid

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  public ServiceRequest actionInfo(List<ActionInfo> actionInfo) {
    this.actionInfo = actionInfo;
    return this;
  }

  public ServiceRequest addActionInfoItem(ActionInfo actionInfoItem) {
    if (this.actionInfo == null) {
      this.actionInfo = new ArrayList<ActionInfo>();
    }
    this.actionInfo.add(actionInfoItem);
    return this;
  }

  /**
   * Get actionInfo
   * @return actionInfo
  **/
  @Valid

  public List<ActionInfo> getActionInfo() {
    return actionInfo;
  }

  public void setActionInfo(List<ActionInfo> actionInfo) {
    this.actionInfo = actionInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceRequest serviceRequest = (ServiceRequest) o;
    return Objects.equals(this.requestInfo, serviceRequest.requestInfo) &&
        Objects.equals(this.services, serviceRequest.services) &&
        Objects.equals(this.actionInfo, serviceRequest.actionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestInfo, services, actionInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceRequest {\n");
    
    sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
    sb.append("    services: ").append(toIndentedString(services)).append("\n");
    sb.append("    actionInfo: ").append(toIndentedString(actionInfo)).append("\n");
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

