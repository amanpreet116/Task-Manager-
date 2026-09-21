package com.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String bucketName,
        String region,
        URI endpoint,
        String accessKey,
        String secretKey,
        long presignedUrlExpirationMinutes
) {
}
