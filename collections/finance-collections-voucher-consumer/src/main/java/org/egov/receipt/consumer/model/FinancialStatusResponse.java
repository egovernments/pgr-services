package org.egov.receipt.consumer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(value = Include.NON_NULL)
public @Data class FinancialStatusResponse {
    private List<FinancialStatus> financialStatuses;
}