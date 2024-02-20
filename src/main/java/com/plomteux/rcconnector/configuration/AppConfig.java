package com.plomteux.rcconnector.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Value("${rcc.api.endpoint.url}")
    private String tempEndpointUrl;
    private static String endpointUrl;

    @PostConstruct
    public void init() {
        endpointUrl = tempEndpointUrl;
    }

    @Bean
    public HttpHeaders jsonHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public static String getBaseUrl() {
        return endpointUrl;
    }
}