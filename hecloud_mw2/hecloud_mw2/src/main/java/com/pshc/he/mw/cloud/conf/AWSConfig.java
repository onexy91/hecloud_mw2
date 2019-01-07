package com.pshc.he.mw.cloud.conf;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Configuration
public class AWSConfig {
	@Value("${cloud.accessKey}")
	private String accessKey;
	@Value("${cloud.secretKey}")
	private String secretKey;
	@Value("${cloud.region}")
	private String region;
	@Value("${cloud.endPoint}")
	private String endPoint;
	
	private AmazonS3 amazonS3;
	@Bean
	public AmazonS3 amazonS3() {
		 amazonS3 = AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
				.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
				.build();
		
		log.info("amazonS3ClientBuilder Initializable");
		
		return amazonS3;
	
	}

}
