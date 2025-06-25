package com.portmanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /** Primary mapper used by Spring MVC for request bodies. */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new ParameterNamesModule()) // <-- key!
                .registerModule(new JavaTimeModule())
                .findAndRegisterModules()
                .disable(com.fasterxml.jackson.databind.SerializationFeature.
                        WRITE_DATES_AS_TIMESTAMPS);
    }
}
