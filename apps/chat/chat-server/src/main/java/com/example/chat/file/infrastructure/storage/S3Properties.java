package com.example.chat.file.infrastructure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws.s3")
@Getter
@Setter
public class S3Properties {

    private String bucketName = "chat-files-local";
    private String region     = "ap-northeast-2";
    private String endpoint   = "";
    private String accessKey  = "";
    private String secretKey  = "";
}
