package org.egov.receipt.consumer.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public @Data class InstrumentRequest {
    private RequestInfo requestInfo = new RequestInfo();
    private List<InstrumentContract> instruments = new ArrayList<InstrumentContract>();
}