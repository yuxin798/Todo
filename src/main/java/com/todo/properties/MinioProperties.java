package com.todo.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
@Data
@ConfigurationProperties(prefix = "trust.minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private Map<String, String> allowedContentType;
}
