package org.egov.workflow.persistence.repository;

import java.util.List;

import org.egov.workflow.web.contract.Employee;
import org.egov.workflow.web.contract.EmployeeRes;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeRepository {

    private final RestTemplate restTemplate;
    private final String employeesByUserIdUrl;
    private final String employeesByPositionIdurl;

    private final String employeesByRoleNameurl;

    @Autowired
    public EmployeeRepository(final RestTemplate restTemplate,
            @Value("${egov.services.eis.host}") final String eisServiceHostname,
            @Value("${egov.services.eis.employee_by_userid}") final String employeesByUserIdUrl,
            @Value("${egov.services.eis.employee_by_position}") final String employeesByPositionIdurl,
            @Value("${egov.services.eis.employee_by_role}") final String employeesByRoleNameurl) {

        this.restTemplate = restTemplate;
        this.employeesByUserIdUrl = eisServiceHostname + employeesByUserIdUrl;
        this.employeesByPositionIdurl = eisServiceHostname + employeesByPositionIdurl;
        this.employeesByRoleNameurl = eisServiceHostname + employeesByRoleNameurl;
    }

    public List<Employee> getByRoleName(final String roleName, final String tenantId) {
        final EmployeeRes employeeRes = restTemplate.getForObject(employeesByRoleNameurl, EmployeeRes.class, roleName,tenantId);
        return employeeRes.getEmployees();
    }

    public EmployeeRes getEmployeeForPositionAndTenantId(final Long posId, final LocalDate asOnDate, final String tenantId) {
        return restTemplate.getForObject(employeesByPositionIdurl, EmployeeRes.class, tenantId, posId, asOnDate);
    }

    public EmployeeRes getEmployeeForUserIdAndTenantId(final Long userId,final String tenantId) {
        return restTemplate.getForObject(employeesByUserIdUrl, EmployeeRes.class, userId, tenantId);
    }
}
