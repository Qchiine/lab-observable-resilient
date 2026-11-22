package com.example.bookservice.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {

    /**
     * Timeouts explicites : sans eux, un pricing lent bloque les threads de book-service
     * jusqu'a epuisement du pool Tomcat.
     *
     * Note : RestTemplateBuilder.connectTimeout(...) a change d'API en Spring Boot 3.3,
     * d'ou l'usage de la RequestFactory, stable quelle que soit la version.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(1));
        factory.setReadTimeout(Duration.ofSeconds(2));
        return builder.requestFactory(() -> factory).build();
    }
}
