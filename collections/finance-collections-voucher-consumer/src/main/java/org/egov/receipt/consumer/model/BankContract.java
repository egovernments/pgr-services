package org.egov.receipt.consumer.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BankContract {

	private Long id;

	@JsonProperty(access = Access.WRITE_ONLY)
	private List<Long> ids = new ArrayList<Long>();

	@NotNull
	@Length(max = 50, min = 1)
	private String code;

	@NotNull
	@Length(max = 100, min = 2)
	private String name;

	@Length(max = 250)
	private String description;

	@NotNull
	private Boolean active;
	// is this required?

	@Length(max = 50)
	private String type;
	
	private String tenantId;

	public Long getId() {
		return id;
	}

	public BankContract(final String id) {
		super();
		this.id = Long.valueOf(id);
	}

	public BankContract(Bank bean) {
		this.setTenantId(bean.getTenantId());
		this.setId(bean.getId());
		this.setCode(bean.getCode());
		this.setName(bean.getName());
		this.setType(bean.getType());
		this.setActive(bean.getActive());
		this.setDescription(bean.getDescription());
	}

}