package org.egov.filestore.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.filestore.domain.exception.EmptyFileUploadRequestException;
import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.domain.model.FileInfo;
import org.egov.filestore.domain.model.FileLocation;
import org.egov.filestore.domain.model.Resource;
import org.egov.filestore.persistence.repository.ArtifactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StorageService {

	private static final String UPLOAD_MESSAGE = "Received upload request for "
			+ "jurisdiction: %s, module: %s, tag: %s with file count: %s";

	private ArtifactRepository artifactRepository;
	private IdGeneratorService idGeneratorService;

	@Autowired
	public StorageService(ArtifactRepository artifactRepository, IdGeneratorService idGeneratorService) {
		this.artifactRepository = artifactRepository;
		this.idGeneratorService = idGeneratorService;
	}

	public List<String> save(List<MultipartFile> filesToStore, String module, String tag, String tenantId) {
		validateFilesToUpload(filesToStore, module, tag, tenantId);
		log.info(UPLOAD_MESSAGE, module, tag, filesToStore.size());
		List<Artifact> artifacts = mapFilesToArtifacts(filesToStore, module, tag, tenantId);
		return this.artifactRepository.save(artifacts);
	}

	private void validateFilesToUpload(List<MultipartFile> filesToStore, String module, String tag, String tenantId) {
		if (CollectionUtils.isEmpty(filesToStore)) {
			throw new EmptyFileUploadRequestException(module, tag, tenantId);
		}
	}

	private List<Artifact> mapFilesToArtifacts(List<MultipartFile> files, String module, String tag, String tenantId) {

		final String folderName = getFolderName(module, tenantId);
		return files.stream().map(file -> {
			String fileName = folderName + file.getOriginalFilename();
			String id = this.idGeneratorService.getId();
			FileLocation fileLocation = new FileLocation(id, module, tag, tenantId, fileName,null);
			return new Artifact(file, fileLocation);
		}).collect(Collectors.toList());
	}

	private String getFolderName(String module, String tenantId) {

		Calendar calendar = Calendar.getInstance();
		return getBucketName(tenantId, calendar) + "/" + getFolderName(module,tenantId, calendar);
	}

	public Resource retrieve(String fileStoreId, String tenantId) throws IOException {
		return artifactRepository.find(fileStoreId, tenantId);
	}

	public List<FileInfo> retrieveByTag(String tag, String tenantId) {
		return artifactRepository.findByTag(tag, tenantId);
	}
	
	private String getFolderName(String module, String tenantId, Calendar calendar) {
		return tenantId + "/" + module + "/" + calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)
				+ "/" + calendar.get(Calendar.DATE) + "/";
	}
	
	private String getBucketName(String tenantId, Calendar calendar) {
		// TODO add config filter logic
		return tenantId.split("\\.")[0] + calendar.get(Calendar.YEAR);
	}
}
