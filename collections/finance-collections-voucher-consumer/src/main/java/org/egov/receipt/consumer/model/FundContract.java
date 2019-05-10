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

@JsonPropertyOrder({ "id", "name", "code", "identifier", "level", "parentId", "isParent", "active" })
public class FundContract extends AuditableContract {

	private Long id;

	@Length(max = 50, min = 2)
	@NotNull
	private String name;

	@Length(max = 50, min = 2)
	@NotNull
	private String code;
	@NotNull
	private Character identifier;

	@NotNull
	private Long level;

	@JsonProperty(access = Access.WRITE_ONLY)
	private FundContract parentId;

	private Boolean isParent;
	@NotNull
	private Boolean active;

	public Long getId() {
		return this.id;
	}

	public FundContract(final String id) {
		super();
		this.id = Long.valueOf(id);
	}

}
