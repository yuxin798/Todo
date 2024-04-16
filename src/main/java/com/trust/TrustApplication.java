package com.trust;

import com.trust.properties.MinioProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MinioProperties.class)
@MapperScan(basePackages = "com.trust.user.mapper")
public class TrustApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrustApplication.class, args);
    }

}
