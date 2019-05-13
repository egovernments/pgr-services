package org.egov.receipt.consumer.model;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Component
public class VoucherIntegrationLogTO {
	int id;
	String referenceNumber;
	String status;
	String voucherNumber;
	String type;
	String requestJson;
	String description;
}
