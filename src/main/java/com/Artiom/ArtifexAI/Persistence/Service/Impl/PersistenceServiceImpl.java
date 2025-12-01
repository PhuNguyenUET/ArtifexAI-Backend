package com.Artiom.ArtifexAI.Persistence.Service.Impl;

import com.Artiom.ArtifexAI.Common.Exception.BusinessException;
import com.Artiom.ArtifexAI.Persistence.Service.PersistenceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class PersistenceServiceImpl implements PersistenceService {
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${aws.cloudfront.keyPairId}")
    private String keyPairId;

    @Value("${aws.cloudfront.privateKeyPath}")
    private String privateKeyPath;

    @Value("${aws.cloudfront.url-access-time}")
    private int accessTimeInHours;

    private S3Client s3Client;
    private CloudFrontUtilities cloudFrontUtilities;

    @PostConstruct
    private void initS3() {
        s3Client = S3Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .build();

        cloudFrontUtilities = CloudFrontUtilities.create();
    }

    @Override
    public String uploadImageToPersistence(byte[] data) {
        String keyName = "server/image_" + System.currentTimeMillis() + ".png";

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType("image/png")
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(data));

        return keyName;
    }

    @Override
    public String getImageUrl(String imagePath) {
        try {
            String resourceUrl = "https://" + cloudFrontDomain + "/" + imagePath;

            Instant expirationTime = Instant.now().plus(accessTimeInHours, ChronoUnit.HOURS);

            Path keyPath = Paths.get(privateKeyPath);

            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                    .resourceUrl(resourceUrl)
                    .privateKey(keyPath)
                    .keyPairId(keyPairId)
                    .expirationDate(expirationTime)
                    .build();

            SignedUrl signedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest);

            return signedUrl.url();
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate CloudFront signed URL");
        }
    }

    @Override
    public byte[] downloadImageFromPersistence(String imagePath) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(imagePath)
                .build();

        ResponseBytes<?> s3ObjectBytes = s3Client.getObjectAsBytes(getRequest);
        return s3ObjectBytes.asByteArray();
    }

    @Override
    public void deleteImageFromPersistence(String imagePath) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(imagePath)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
