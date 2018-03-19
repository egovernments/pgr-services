package org.egov.filestore.persistence.repository;

import org.egov.filestore.persistence.entity.Artifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileStoreJpaRepository extends JpaRepository<Artifact, Long> {
	Artifact findByFileStoreIdAndTenantId(String fileStoreId, String tenantId);

	List<Artifact> findByTagAndTenantId(String tag, String tenantId);
}
