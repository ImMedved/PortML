package com.portmanager.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * RestTemplate bean with timeouts that can be
 * set via environment variables:
 * <p>
 * ML_CONNECT_TIMEOUT – connection, sec (default 5)
 * ML_READ_TIMEOUT – response wait, sec (default 15)
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        long connectSec = Optional.ofNullable(System.getenv("ML_CONNECT_TIMEOUT"))
                .map(Long::parseLong).orElse(5L);

        long readSec    = Optional.ofNullable(System.getenv("ML_READ_TIMEOUT"))
                .map(Long::parseLong).orElse(15L);

        return builder
                .setConnectTimeout(Duration.ofSeconds(connectSec))
                .setReadTimeout   (Duration.ofSeconds(readSec))
                .build();
    }
}
