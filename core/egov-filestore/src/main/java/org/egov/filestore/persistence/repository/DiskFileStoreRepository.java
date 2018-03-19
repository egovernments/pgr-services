package org.egov.filestore.persistence.repository;

import org.egov.filestore.domain.model.Artifact;
import org.egov.filestore.domain.model.FileLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DiskFileStoreRepository {

    @Autowired
    private AwsS3Repository s3Repository;
    
	@Value("${isS3Enabled}")
	private Boolean isS3Enabled;
    
	private FileRepository fileRepository;
    
    private String fileMountPath;

    public DiskFileStoreRepository(FileRepository fileRepository,
                                   @Value("${file.storage.mount.path}") String fileMountPath) {
        this.fileRepository = fileRepository;
        this.fileMountPath = fileMountPath;
    }

	public void write(List<Artifact> artifacts) {
		artifacts.forEach(artifact -> {
			MultipartFile multipartFile = artifact.getMultipartFile();
			FileLocation fileLocation = artifact.getFileLocation();
			if (isS3Enabled) {
				s3Repository.writeToS3(multipartFile, fileLocation);
			} else {
				Path path = getPath(fileLocation);
				fileRepository.write(multipartFile, path);
			}
		});
	}

	public Resource read(FileLocation fileLocation) {
		
		Resource resource = null;
		
		if(!isS3Enabled) {
        Path path = getPath(fileLocation);
        resource = fileRepository.read(path);
		}else {
			resource = s3Repository.getObject(fileLocation.getFileName());
		}
        
        return resource;
    }

	private Path getPath(FileLocation fileLocation) {
		return Paths.get(fileMountPath, fileLocation.getFileName());
	}


}

