package org.egov.receipt.consumer.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InstrumentType{

	/*
	 * id is the unique reference to instrument type entered in the system.
	 */
	private String id;

	/*
	 * type specifies the mode/type of transaction that can be made - i.e
	 * Cheque,DD,RTGS. For receipt - Cheque,DD,RTGS
	 */
	@NotNull
	@NotBlank
	@Size(max = 50, min = 2)
	private String name;

	/*
	 * description specifies details of the instrument type . For example type
	 * DD description may be Demand Draft
	 */

	@Size(max = 100)
	private String description;

	/*
	 * active specifies whether the type is active for transacting.
	 */
	@NotNull
	private Boolean active;

	@Valid
	@NotNull
	@Size(max = 2, min = 2, message = "")
	private List<InstrumentTypeProperty> instrumentTypeProperties;

}

