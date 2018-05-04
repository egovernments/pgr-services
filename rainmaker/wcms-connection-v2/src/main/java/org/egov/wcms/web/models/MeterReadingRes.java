package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.egov.wcms.web.models.ActionHistory;
import org.egov.wcms.web.models.MeterReading;
import org.egov.wcms.web.models.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * MeterReadingRes
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeterReadingRes   {
        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo = null;

        @JsonProperty("meterReading")
        @Valid
        private List<MeterReading> meterReading = null;

        @JsonProperty("actionHistory")
        @Valid
        private List<ActionHistory> actionHistory = null;


        public MeterReadingRes addMeterReadingItem(MeterReading meterReadingItem) {
            if (this.meterReading == null) {
            this.meterReading = new ArrayList<>();
            }
        this.meterReading.add(meterReadingItem);
        return this;
        }

        public MeterReadingRes addActionHistoryItem(ActionHistory actionHistoryItem) {
            if (this.actionHistory == null) {
            this.actionHistory = new ArrayList<>();
            }
        this.actionHistory.add(actionHistoryItem);
        return this;
        }

}

