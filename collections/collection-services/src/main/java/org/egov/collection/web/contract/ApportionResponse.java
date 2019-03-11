package org.egov.collection.web.contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class ApportionResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	private String tenantId;

	private String businessService;

	private List<Bill> bills;

	@JsonProperty("collectionMap")
	@Valid
	private Map<String, BigDecimal> collectionMap;
}
