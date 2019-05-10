package org.egov.receipt.consumer.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BillDetail {
	private String id;

    private String bill;

    private Long billDate;

    private String billDescription;

    private String billNumber; // refNo

    private String consumerCode;

    private String consumerType;

    private BigDecimal minimumAmount;

    private BigDecimal totalAmount;

    private BigDecimal collectedAmount;

    private List<String> collectionModesNotAllowed = new ArrayList<>();

    private String tenantId;

    private String businessService; // buisnessDetailsCode

    private String displayMessage;

    private Boolean callBackForApportioning;

    private String receiptNumber;

    private Long receiptDate;

    private String receiptType;

    private String channel;

    private String voucherHeader;

//    private CollectionType collectionType;

    private String boundary;

    private String reasonForCancellation;

    private BigDecimal amountPaid;

    private String cancellationRemarks;

    private String status;

    @JsonProperty("billAccountDetails")
    private List<BillAccountDetail> billAccountDetails = new ArrayList<>();

    private String manualReceiptNumber;

    private Long manualReceiptDate;

    private Long stateId;

    private Boolean partPaymentAllowed;
}
