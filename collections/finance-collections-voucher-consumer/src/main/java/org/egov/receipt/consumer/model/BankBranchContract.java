package org.egov.receipt.consumer.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@JsonPropertyOrder({ "id", "code", "name", "bank", "address", "address2", "city", "state", "pincode", "phone", "fax",
		"contactPerson", "active", "description", "micr", "bankAccounts" })
public class BankBranchContract extends AuditableContract {

	private Long id;

	@NotNull
	private BankContract bank;

	@NotNull
	@Length(max = 50, min = 1)
	private String code;

	@NotNull
	@Length(max = 50, min = 1)
	@Pattern(regexp = "^[a-zA-Z0-9_]*$")
	private String name;

	@NotNull
	@Length(max = 50, min = 1)
	private String address;

	@Length(max = 50)
	private String address2;

	@Length(max = 50)
	private String city;

	@Length(max = 50)
	private String state;

	@Length(max = 50)
	private String pincode;

	@Length(max = 15)
	private String phone;

	@Length(max = 15)
	private String fax;

	@Length(max = 50)
	private String contactPerson;

	@NotNull
	private Boolean active;

	@Length(max = 256)
	private String description;

	@Length(max = 50)
	private String micr;

	public Long getId() {
		return this.id;
	}

	public BankBranchContract(final String id) {
		super();
		this.id = Long.valueOf(id);
	}


}
