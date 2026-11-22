package com.example.bookservice.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

/**
 * Expose l'etat Resilience4j en JSON : pratique pour un tableau de bord
 * (le endpoint Actuator natif n'est pas lisible par un humain).
 */
@RestController
public class DiagnosticController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Value("${pricing.base-url}")
    private String pricingBaseUrl;

    public DiagnosticController(CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @GetMapping("/api/diagnostic/resilience")
    public Map<String, Object> resilience() {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("pricingBaseUrl", pricingBaseUrl);

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("pricing");
        Map<String, Object> etatCb = new LinkedHashMap<>();
        etatCb.put("state", cb.getState().name());
        etatCb.put("failureRate", cb.getMetrics().getFailureRate());
        etatCb.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
        etatCb.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
        etatCb.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
        etatCb.put("slowCalls", cb.getMetrics().getNumberOfSlowCalls());
        reponse.put("circuitBreaker", etatCb);

        Retry retry = retryRegistry.retry("pricing");
        Map<String, Object> etatRetry = new LinkedHashMap<>();
        etatRetry.put("failedCallsWithRetryAttempt", retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
        etatRetry.put("failedCallsWithoutRetryAttempt", retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
        etatRetry.put("successfulCallsWithRetryAttempt", retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
        reponse.put("retry", etatRetry);

        return reponse;
    }
}
