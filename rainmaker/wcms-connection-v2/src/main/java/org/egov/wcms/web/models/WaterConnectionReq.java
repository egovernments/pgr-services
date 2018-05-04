package org.egov.wcms.web.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.egov.common.contract.request.RequestInfo;


/**
 * WaterConnectionReq
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaterConnectionReq   {
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        @JsonProperty("connections")
        @Valid
        @NotNull
        private List<Connection> connections = new ArrayList<>();

        @JsonProperty("actionInfo")
        @Valid
        private List<ActionInfo> actionInfo = null;


        public WaterConnectionReq addConnectionsItem(Connection connectionsItem) {
        this.connections.add(connectionsItem);
        return this;
        }

        public WaterConnectionReq addActionInfoItem(ActionInfo actionInfoItem) {
            if (this.actionInfo == null) {
            this.actionInfo = new ArrayList<>();
            }
        this.actionInfo.add(actionInfoItem);
        return this;
        }

}

