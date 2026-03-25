package com.example.chat.file.infrastructure.storage;

import com.example.chat.file.domain.model.S3Key;
import com.example.chat.file.domain.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.net.URI;

/** AWS SDK v2 기반 S3 업로드 구현체 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements S3StorageService {

    private final S3Properties s3Properties;
    private S3Client s3Client;

    @PostConstruct
    void init() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3Properties.getRegion()));

        String accessKey = s3Properties.getAccessKey();
        String secretKey = s3Properties.getSecretKey();
        if (accessKey != null && !accessKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)));
        }

        String endpoint = s3Properties.getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                   .forcePathStyle(true);
        }

        this.s3Client = builder.build();
    }

    @Override
    public String upload(S3Key s3Key, byte[] bytes, String mimeType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(s3Key.value())
                .contentType(mimeType)
                .contentLength((long) bytes.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));

        return buildPublicUrl(s3Key);
    }

    private String buildPublicUrl(S3Key s3Key) {
        String endpoint = s3Properties.getEndpoint();
        if (endpoint != null && !endpoint.isBlank()) {
            // MinIO local
            return endpoint + "/" + s3Properties.getBucketName() + "/" + s3Key.value();
        }
        return "https://" + s3Properties.getBucketName()
                + ".s3." + s3Properties.getRegion()
                + ".amazonaws.com/" + s3Key.value();
    }
}
