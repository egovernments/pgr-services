package org.egov.receipt.consumer.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Bill {
	
	  @JsonProperty("id")
	  private String id = null;

	  @JsonProperty("mobileNumber")
	  private String mobileNumber = null;
	  
	  @JsonProperty("paidBy")
	  private String paidBy = null;

	  @JsonProperty("payerName")
	  private String payerName = null;

	  @JsonProperty("payerAddress")
	  private String payerAddress = null;

	  @JsonProperty("payerEmail")
	  private String payerEmail = null;
	  
	  @JsonProperty("payerId")
	  private String payerId = null;

	  @JsonProperty("isActive")
	  private Boolean isActive = null;

	  @JsonProperty("isCancelled")
	  private Boolean isCancelled = null;

	  @JsonProperty("additionalDetails")
	  private Object additionalDetails = null;

	  @JsonProperty("taxAndPayments")
	  @Valid
	  @NotNull
	  private List<TaxAndPayment> taxAndPayments = null;

	  @JsonProperty("billDetails")
	  @Valid
	  private List<BillDetail> billDetails = null;

	  @JsonProperty("tenantId")
	  private String tenantId = null;

	  @JsonProperty("auditDetails")
	  private AuditDetails auditDetails = null;

}
