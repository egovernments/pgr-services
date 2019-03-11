package org.egov.hrms.model;

import java.util.Map;

import org.egov.hrms.web.contract.EmployeeRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
	
	private EmployeeRequest request;
	
	private Map<String, String> pwdMap;

}
