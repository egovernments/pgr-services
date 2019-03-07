package org.egov.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * BillInfo
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2019-02-25T15:07:36.183+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillInfo {
    @JsonProperty("id")
    private BigDecimal id = null;

    @JsonProperty("payerName")
    private String payerName = null;

    @JsonProperty("payerAddress")
    private String payerAddress = null;

    @JsonProperty("payerEmail")
    private String payerEmail = null;

    @JsonProperty("paidBy")
    private String paidBy = null;

    @JsonProperty("isActive")
    private Boolean isActive = null;

    @JsonProperty("isCancelled")
    private Boolean isCancelled = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("mobileNumber")
    private String mobileNumber = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    @JsonProperty("billDetails")
    @Valid
    private List<BillDetail> billDetails = null;

    @Size(min=1)
    @JsonProperty("taxAndPayments")
    private List<TaxAndPayment> taxAndPayments = null;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;


    public BillInfo addBillDetailsItem(BillDetail billDetailsItem) {
        if (this.billDetails == null) {
            this.billDetails = new ArrayList<>();
        }
        this.billDetails.add(billDetailsItem);
        return this;
    }


    public BillInfo addTaxAndPayment(TaxAndPayment taxAndPaymentItem) {
        if (this.taxAndPayments == null) {
            this.taxAndPayments = new ArrayList<>();
        }
        this.taxAndPayments.add(taxAndPaymentItem);
        return this;
    }


}

