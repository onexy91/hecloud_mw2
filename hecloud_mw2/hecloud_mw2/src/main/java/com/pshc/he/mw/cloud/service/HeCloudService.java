package com.pshc.he.mw.cloud.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.pshc.he.cloud.api.CloudFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HeCloudService {
	@Autowired
	private AmazonS3 amazonS3;

	public void getBucketList(List<Map<String, Object>> ObjectList) {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}
		List<Bucket> bucketList = amazonS3.listBuckets();
		Map<String, Object> map;

		for (Bucket bucket : bucketList) {
			map = new HashMap<String, Object>();
			map.put("BucketName", bucket.getName());
			ObjectList.add(map);
		}
	}

	// upload object
	public void uploadFile(String bucketName, String fileName, File file) {// 최대 5TB
		if (amazonS3 != null) {
			try {
				PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file);

				// file permission
				putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

				// upload file
				PutObjectResult ret = amazonS3.putObject(putObjectRequest);
				log.info("ret: " + ret.getETag());
				// System.out.println("ret: " + ret.getETag());

			} catch (AmazonServiceException ase) {
				ase.printStackTrace();
			} finally {
				amazonS3 = null;
			}
		}
	}

	public void getFolderList(String bucketName, List<Map<String, Object>> objectList) {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}
		List<CloudFile> cloudFileList = new ArrayList<CloudFile>();

		try {
			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
					.withDelimiter("/").withMaxKeys(300);
			ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);
			log.info("Foler List:");
			for (String commonPrefixes : objectListing.getCommonPrefixes()) {
				CloudFile cloudFile = new CloudFile(commonPrefixes);
				log.info("    name=" + cloudFile.name);
				cloudFileList.add(cloudFile);
			}

			for (CloudFile cloudFile : cloudFileList) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", cloudFile.getName());
				objectList.add(map);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void getObjectList(String bucketName, List<Map<String, Object>> objectList) {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}
		List<CloudFile> cloudFileList = new ArrayList<CloudFile>();
		CloudFile cloudFile;
		try {
			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
					.withDelimiter("/").withMaxKeys(300);

			ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);
			log.info("Folder List:");
			for (String commonPrefixes : objectListing.getCommonPrefixes()) {
				cloudFile = new CloudFile(commonPrefixes);
				log.info("    name=" + cloudFile.getName());
				cloudFileList.add(cloudFile);
			}

			log.info("file List:");
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				cloudFile = new CloudFile(objectSummary.getKey());
				cloudFile.setSize(Long.toString(objectSummary.getSize()));
				cloudFile.setLastModified(objectSummary.getLastModified().toString());
				log.info("    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize() + ", owner="
						+ objectSummary.getOwner().getId());
				cloudFileList.add(cloudFile);
			}
		} catch (AmazonS3Exception e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
	}

	public void getFolderToFileList(String bucketName, String folderCode, List<Map<String, Object>> objectList) {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}

		try {
			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
					.withPrefix(folderCode).withMaxKeys(300);

			ObjectListing objectListing = amazonS3.listObjects(listObjectsRequest);
			log.info("File List:");
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				Map<String, Object> objectMap = new HashMap<String, Object>();
				objectMap.put("objectList", objectSummary.getKey());
				objectList.add(objectMap);
				log.info("    name=" + objectSummary.getKey() + ", size=" + objectSummary.getSize() + ", owner="
						+ objectSummary.getOwner().getId());
			}

		} catch (AmazonS3Exception e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
	}

	public void multipleUpload(String bucketName, List<File> objectList) {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}

		try {
			for (File objectFile : objectList) {
				PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectFile.getName(), objectFile);
				// File premission
				putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

				// upload file
				PutObjectResult ret = amazonS3.putObject(putObjectRequest);
				System.out.println("ret: " + ret.getETag());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// download object
	public void downloadFile(String bucketName, String savePath, String studyUid, OutputStream outputStream)
			throws IOException {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}
		log.info(savePath + studyUid);
		S3ObjectInputStream s3objectInputStream = null;

		try {
			S3Object s3Object = amazonS3.getObject(bucketName, savePath + studyUid);
			s3objectInputStream = s3Object.getObjectContent();

			byte[] bytesArray = new byte[4096];
			int bytesRead = -1;

			while ((bytesRead = s3objectInputStream.read(bytesArray)) != -1) {
				outputStream.write(bytesArray, 0, bytesRead);
			}
			outputStream.flush();

			log.debug("Object %s has been downloaded.\n", studyUid);
			// System.out.format("Object %s has been downloaded.\n", objectName);
		} catch (AmazonS3Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (s3objectInputStream != null)
					s3objectInputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		}
	}

	private void makeFolder(String bucket) {
		if (amazonS3 == null) {
			throw new NullPointerException();
		}
		String key = "1121/";
		InputStream is = new ByteArrayInputStream(new byte[0]);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		amazonS3.putObject(new PutObjectRequest(bucket, key, is, metadata));

	}
}
