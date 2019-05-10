package org.egov.receipt.consumer.model;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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

@JsonPropertyOrder({ "id", "bankBranch", "chartOfAccount", "fund", "accountNumber", "accountType", "description",
		"active", "payTo", "type" })
public class BankAccountContract extends AuditableContract implements java.io.Serializable {

	private Long id;

	private BankBranchContract bankBranch;

	@JsonProperty(access = Access.WRITE_ONLY)
	private ChartOfAccount chartOfAccount;

	@JsonProperty(access = Access.WRITE_ONLY)
	private FundContract fund;

	@NotNull
	@Length(max = 25)
	private String accountNumber;

	// is this required ?
	private String accountType;
	@Length(max = 256)
	private String description;

	@NotNull
	private Boolean active;

	@Length(max = 100)
	private String payTo;

	@NotNull
	private BankAccountType type;

	public Long getId() {
		return this.id;
	}

}
