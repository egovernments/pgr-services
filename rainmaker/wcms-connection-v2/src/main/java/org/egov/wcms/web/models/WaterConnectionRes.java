package org.egov.wcms.web.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.egov.common.contract.response.ResponseInfo;


/**
 * WaterConnectionRes
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaterConnectionRes {
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo = null;

	@JsonProperty("connections")
	@Valid
	private List<Connection> connections = null;

	@JsonProperty("actionHistory")
	@Valid
	private List<ActionHistory> actionHistory = null;

	public WaterConnectionRes addConnectionsItem(Connection connectionsItem) {
		if (this.connections == null) {
			this.connections = new ArrayList<>();
		}
		this.connections.add(connectionsItem);
		return this;
	}

	public WaterConnectionRes addActionHistoryItem(ActionHistory actionHistoryItem) {
		if (this.actionHistory == null) {
			this.actionHistory = new ArrayList<>();
		}
		this.actionHistory.add(actionHistoryItem);
		return this;
	}

}
