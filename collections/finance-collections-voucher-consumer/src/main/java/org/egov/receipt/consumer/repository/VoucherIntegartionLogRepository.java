package org.egov.receipt.consumer.repository;

import org.egov.receipt.consumer.entity.VoucherIntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface VoucherIntegartionLogRepository extends JpaRepository<VoucherIntegrationLog, Integer>{

}
