package org.egov.receipt.consumer.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class InstrumentStatus {

	/**
	 * Unique Identifier of the status
	 */
	@NotNull
	private String id;

	@NotNull
	@Size(min = 3, max = 50)
	private String moduleType;

	/**
	 * name is the status name 
	 */
	@NotNull
	@Size(min = 3, max = 20)
	private String name;

	/**
	 * description is the detailed description of the status
	 */
	@NotNull
	@Size(min = 3, max = 250)
	private String description;

}