package com.example.bookservice.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.retry.annotation.Retry;

/**
 * Couche d'appel HTTP brute, protegee par le Retry uniquement.
 * Isolee dans un composant pour que le proxy AOP Resilience4j s'applique
 * (un appel interne "this.xxx()" ne passe pas par le proxy).
 */
@Component
public class PricingHttpCaller {

    private static final ParameterizedTypeReference<Map<String, Object>> TYPE_PRIX =
            new ParameterizedTypeReference<>() { };

    private final RestTemplate rest;
    private final String baseUrl;

    public PricingHttpCaller(RestTemplate rest, @Value("${pricing.base-url}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    @Retry(name = "pricing")
    public double appel(long bookId) {
        String url = baseUrl + "/api/prices/" + bookId;
        ResponseEntity<Map<String, Object>> reponse =
                rest.exchange(url, HttpMethod.GET, null, TYPE_PRIX);
        Object prix = reponse.getBody() == null ? null : reponse.getBody().get("price");
        if (prix == null) {
            throw new IllegalStateException("Reponse pricing sans prix");
        }
        return ((Number) prix).doubleValue();
    }
}
