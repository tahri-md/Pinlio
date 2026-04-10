package com.pinlio.pinservice.service;

import com.pinlio.pinservice.config.S3Properties;
import com.pinlio.pinservice.dto.PresignUploadRequest;
import com.pinlio.pinservice.dto.PresignUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public PresignUploadResponse createPresignedUpload(UUID requesterId, PresignUploadRequest request) {
        String contentType = normalizeContentType(request.getContentType());
        String imageKey = buildImageKey(requesterId, request.getFileName());
        Duration expiry = presignExpiry();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket())
                .key(imageKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(expiry)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(putObjectPresignRequest);
        Instant expiresAt = Instant.now().plus(expiry);

        return PresignUploadResponse.builder()
                .imageKey(imageKey)
                .uploadUrl(presignedPutObjectRequest.url().toString())
                .expiresAt(expiresAt)
                .build();
    }

    public String generateDownloadUrl(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return null;
        }

        Duration expiry = presignExpiry();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket())
                .key(imageKey)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(expiry)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url().toString();
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content type is required");
        }

        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are allowed");
        }
        return normalized;
    }

    private String buildImageKey(UUID requesterId, String fileName) {
        String safeFileName = sanitizeFileName(fileName);
        return "users/" + requesterId + "/pins/" + UUID.randomUUID() + "-" + safeFileName;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
        }

        return fileName.trim()
                .replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-+", "-");
    }

    private Duration presignExpiry() {
        long seconds = s3Properties.getPresignExpirySeconds();
        if (seconds <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid S3 presign expiry configuration");
        }
        return Duration.ofSeconds(seconds);
    }

    private String bucket() {
        String bucket = s3Properties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 bucket is not configured");
        }
        return bucket.trim();
    }
}