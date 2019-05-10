package org.egov.receipt.consumer.model;

import javax.validation.constraints.NotNull;

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

@JsonPropertyOrder({ "id", "glcode", "name", "accountCodePurpose", "description", "isActiveForPosting", "parentId",
		"type", "classification", "functionRequired", "budgetCheckRequired", "majorCode", "isSubLedger" })
public class ChartOfAccountContract extends AuditableContract {

	private String id;

	@NotNull
	@Length(max = 16, min = 1)
	private String glcode;

	@NotNull
	@Length(max = 128, min = 5)
	private String name;

	private AccountCodePurposeContract accountCodePurpose;

	@Length(max = 256)
	private String description;

	@NotNull
	private Boolean isActiveForPosting;

	private ChartOfAccountContract parentId;

	@NotNull
	private Character type;

	@NotNull
	private Long classification;

	@NotNull
	private Boolean functionRequired;

	@NotNull
	private Boolean budgetCheckRequired;

	@Length(max = 16)
	private String majorCode;

	private Boolean isSubLedger;

	public ChartOfAccountContract(String id) {
		this.id = id;
	}

}