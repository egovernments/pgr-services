package org.egov.wcms.web.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaterConnectionSearchCriteria {
	
	@JsonProperty("tenantId")
	private String tenantId;
	
	@JsonProperty("legacyConsumerNumber")
	private List<Long> legacyConsumerNumber;
	
	@JsonProperty("acknowledgmentNumber")
	private List<String> acknowledgmentNumber;
	
	@JsonProperty("connectionNumber")
	private List<String> connectionNumber;
	
	@JsonProperty("ownerName")
	private List<String> ownerName;
	
	@JsonProperty("phone")
	private List<String> phone;


}
