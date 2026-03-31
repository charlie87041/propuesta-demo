package com.cookiesstore.admin.service.customers;

import com.cookiesstore.common.storage.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerAvatarStorageService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSIONS = Map.of(
        "image/jpeg", ".jpg",
        "image/png", ".png",
        "image/webp", ".webp"
    );

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public CustomerAvatarStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public String uploadCustomerAvatar(Long customerId, MultipartFile file) {
        validateFile(file);
        String contentType = file.getContentType();
        String extension = EXTENSIONS.get(contentType);
        String objectKey = "customers/" + customerId + "/avatar-" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            ensureBucketExists();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.getCustomerAvatarsBucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );
            return objectKey;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to upload customer avatar", ex);
        }
    }

    public AvatarObject loadAvatar(String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioProperties.getCustomerAvatarsBucket())
                    .object(objectKey)
                    .build()
            );

            InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioProperties.getCustomerAvatarsBucket())
                    .object(objectKey)
                    .build()
            );
            return new AvatarObject(inputStream, stat.contentType());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load customer avatar", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Avatar file is too large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Avatar file type is not allowed");
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(minioProperties.getCustomerAvatarsBucket())
                .build()
        );
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(minioProperties.getCustomerAvatarsBucket())
                    .build()
            );
        }
    }

    public record AvatarObject(InputStream stream, String contentType) {
    }
}
