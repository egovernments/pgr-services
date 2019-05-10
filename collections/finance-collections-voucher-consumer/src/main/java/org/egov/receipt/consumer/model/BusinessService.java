package org.egov.receipt.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessService {
	private String code;
	private boolean voucherCreationEnabled;
	private String fund;
	private String function;
	private String department;
	private String functionary;
	private String scheme;
	private String subscheme;
	private Long validFrom;
	private Long validTo;
}
