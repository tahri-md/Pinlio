package com.pinlio.pinservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {
    private String bucket;
    private String region;
    private long presignExpirySeconds = 900;
}