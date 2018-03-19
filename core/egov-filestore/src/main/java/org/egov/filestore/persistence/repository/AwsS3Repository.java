package org.egov.filestore.persistence.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;

import org.egov.filestore.domain.model.FileLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
/**
 * 
 * @author kaviyarasan1993
 *
 */
public class AwsS3Repository {
	
	@Value("${aws.key}")
	private String key;
	
	@Value("${aws.secretkey}")
	private String secretKey;
	
	@Value("${temp.folder.path}")
	private String tempFolder;
	
	private final String TEMP_NAME = "localFile";

	public void writeToS3(MultipartFile file, FileLocation fileLocation) {

		AWSCredentials credentials = new BasicAWSCredentials(key,secretKey);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();

		
		String completeName = fileLocation.getFileName();
		
		int index = completeName.indexOf('/');
		String bucketName = completeName.substring(0,index);
		String fileNameWithPath = completeName.substring(index+1,completeName.length());

		if (!s3Client.doesBucketExistV2(bucketName))
			s3Client.createBucket(bucketName);

		s3Client.listBuckets().forEach(e -> log.info(" the name of the bucket : " + e.getName()));

		InputStream is = null;
		long contentLength = file.getSize();

		try {
			is = file.getInputStream();
		} catch (IOException e) {
			log.error(" exception occured while reading input stream from file ", e);
			throw new RuntimeException(e);
		}

		ObjectMetadata objMd = new ObjectMetadata();
		objMd.setContentLength(contentLength);
		
		s3Client.putObject(bucketName, fileNameWithPath, is, objMd);

		ObjectListing objectListing = s3Client.listObjects(bucketName);
		for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
			log.info(os.getKey());
		}
	}
	
	public Resource getObject(String completeName) {

		long startTime = new Date().getTime();
		
		AWSCredentials credentials = new BasicAWSCredentials(key,secretKey);
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();
		
		int index = completeName.indexOf('/');
		String bucketName = completeName.substring(0,index);
		String fileNameWithPath = completeName.substring(index+1,completeName.length());
		
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, fileNameWithPath);

		long beforeCalling = new Date().getTime();
		
		File localFile = new File(tempFolder+"/"+TEMP_NAME);
		s3Client.getObject(getObjectRequest, localFile);

		long afterAws = new Date().getTime();
		
		FileSystemResource fileSystemResource = new FileSystemResource(Paths.get(tempFolder+"/"+TEMP_NAME).toFile());
		
		long generateResource = new Date().getTime();
		
		log.info(" the time to prep Obj : "+(beforeCalling-startTime));
		log.info(" the time to get object from aws "+(afterAws-beforeCalling));
		log.info(" the time for creating resource form file : "+(generateResource-afterAws));
		return fileSystemResource;
	}
}
