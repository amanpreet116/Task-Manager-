package com.taskmanager.service;

import com.taskmanager.config.S3Properties;
import com.taskmanager.exception.FileStorageException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

/**
 * Binary file contents live in S3, while searchable relational metadata lives
 * in Postgres. This avoids bloating database rows with large byte arrays while
 * preserving task relationships and file details in transactional storage.
 */
@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;
    private volatile boolean bucketReady;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3Properties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    public void upload(String key, MultipartFile file) {
        ensureBucketExists();
        String contentType = file.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.getContentType();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );
        } catch (IOException | RuntimeException exception) {
            throw new FileStorageException("Could not upload file", exception);
        }
    }

    /**
     * A presigned URL grants temporary access to one private object. The
     * signature expires after 15 minutes, avoiding permanent public bucket
     * access and limiting the impact if a URL is shared.
     */
    public String createDownloadUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(
                                properties.presignedUrlExpirationMinutes()))
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    private synchronized void ensureBucketExists() {
        if (bucketReady) {
            return;
        }

        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(properties.bucketName())
                    .build());
        } catch (S3Exception exception) {
            if (exception.statusCode() != 404) {
                throw new FileStorageException(
                        "Could not access the attachment bucket",
                        exception
                );
            }
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(properties.bucketName())
                    .build());
        }

        bucketReady = true;
    }
}
