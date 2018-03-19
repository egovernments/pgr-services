package org.egov.filestore.persistence.repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.filestore.domain.exception.ArtifactNotFoundException;
import org.egov.filestore.domain.model.FileInfo;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.domain.model.Resource;
import org.egov.filestore.persistence.entity.Artifact;
import org.springframework.stereotype.Service;

@Service
public class ArtifactRepository {

	private DiskFileStoreRepository diskFileStoreRepository;
	private FileStoreJpaRepository fileStoreJpaRepository;

	public ArtifactRepository(DiskFileStoreRepository diskFileStoreRepository,
			FileStoreJpaRepository fileStoreJpaRepository) {
		this.diskFileStoreRepository = diskFileStoreRepository;
		this.fileStoreJpaRepository = fileStoreJpaRepository;
	}

	public List<String> save(List<org.egov.filestore.domain.model.Artifact> artifacts) {
		
		List<Artifact> artifactEntities = diskFileStoreRepository.write(artifacts);
		return fileStoreJpaRepository.save(artifactEntities).stream()
				.map(Artifact::getFileStoreId)
				.collect(Collectors.toList());
	}

/*	private List<Artifact> mapArtifactsListToEntitiesList(List<org.egov.filestore.domain.model.Artifact> artifacts) {
		return artifacts.stream()
				.map(this::mapToEntity)
				.collect(Collectors.toList());
	}

	private Artifact mapToEntity(org.egov.filestore.domain.model.Artifact artifact) {

		FileLocation fileLocation = artifact.getFileLocation();
		return Artifact.builder().fileStoreId(fileLocation.getFileStoreId()).fileName(fileLocation.getFileName())
				.contentType(artifact.getMultipartFile().getContentType()).module(fileLocation.getModule())
				.tag(fileLocation.getTag()).tenantId(fileLocation.getTenantId()).build();
	}*/

	public Resource find(String fileStoreId, String tenantId) throws IOException {
		Artifact artifact = fileStoreJpaRepository.findByFileStoreIdAndTenantId(fileStoreId, tenantId);
		if (artifact == null)
			throw new ArtifactNotFoundException(fileStoreId);

		org.springframework.core.io.Resource resource = diskFileStoreRepository.read(artifact.getFileLocation());
		return new Resource(artifact.getContentType(), artifact.getFileName(), resource, artifact.getTenantId(),""+resource.getFile().length()+" bytes");
	}

	public List<FileInfo> findByTag(String tag, String tenantId) {
		return fileStoreJpaRepository.findByTagAndTenantId(tag, tenantId).stream().map(this::mapArtifactToFileInfo)
				.collect(Collectors.toList());
	}

	private FileInfo mapArtifactToFileInfo(Artifact artifact) {
		FileLocation fileLocation = new FileLocation(artifact.getFileStoreId(), artifact.getModule(),
				 artifact.getTag(),artifact.getTenantId(),artifact.getFileName(),artifact.getFileSource());

		return new FileInfo(artifact.getContentType(), fileLocation, artifact.getTenantId());
	}
}
