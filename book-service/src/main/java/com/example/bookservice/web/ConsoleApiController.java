package com.example.bookservice.web;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.bookservice.domain.Book;
import com.example.bookservice.repo.BookRepository;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

/**
 * API de la console d'observabilite (fonctionnalite additionnelle au TP).
 * Regroupe en une seule reponse : etat de l'instance, etat Resilience4j,
 * etat du service pricing (avec bascule chaos depuis l'UI).
 */
@RestController
@RequestMapping("/api/console")
public class ConsoleApiController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;

    @Value("${pricing.base-url}")
    private String pricingBaseUrl;

    @Value("${spring.profiles.active:default}")
    private String profil;

    public ConsoleApiController(CircuitBreakerRegistry circuitBreakerRegistry,
                                RetryRegistry retryRegistry,
                                BookRepository bookRepository,
                                RestTemplate restTemplate) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.bookRepository = bookRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/state")
    public Map<String, Object> state() {
        Map<String, Object> reponse = new LinkedHashMap<>();

        Map<String, Object> instance = new LinkedHashMap<>();
        instance.put("hostname", System.getenv().getOrDefault("HOSTNAME", "local"));
        instance.put("internalPort", System.getenv().getOrDefault("SERVER_PORT", "8081"));
        instance.put("profile", profil);
        instance.put("uptimeSeconds",
                ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
        reponse.put("instance", instance);

        reponse.put("circuitBreaker", etatCircuitBreaker());
        reponse.put("retry", etatRetry());
        reponse.put("pricing", etatPricing());
        reponse.put("books", resumeLivres());
        return reponse;
    }

    /** Bascule la panne simulee de pricing-service depuis la console. */
    @PostMapping("/chaos")
    public Map<String, Object> chaos(@RequestParam(name = "enabled") boolean enabled,
                                     @RequestParam(name = "percent", defaultValue = "100") int percent) {
        String url = pricingBaseUrl + "/api/prices/chaos?enabled=" + enabled + "&percent=" + percent;
        Map<String, Object> reponse = new LinkedHashMap<>();
        try {
            restTemplate.postForObject(url, null, Map.class);
            reponse.put("ok", true);
        } catch (Exception e) {
            reponse.put("ok", false);
            reponse.put("erreur", e.getClass().getSimpleName());
        }
        reponse.put("pricingBaseUrl", pricingBaseUrl);
        return reponse;
    }

    private Map<String, Object> etatCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("pricing");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("state", cb.getState().name());
        map.put("failureRate", cb.getMetrics().getFailureRate());
        map.put("bufferedCalls", cb.getMetrics().getNumberOfBufferedCalls());
        map.put("failedCalls", cb.getMetrics().getNumberOfFailedCalls());
        map.put("successfulCalls", cb.getMetrics().getNumberOfSuccessfulCalls());
        return map;
    }

    private Map<String, Object> etatRetry() {
        Retry retry = retryRegistry.retry("pricing");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("failedCallsWithRetryAttempt", retry.getMetrics().getNumberOfFailedCallsWithRetryAttempt());
        map.put("failedCallsWithoutRetryAttempt", retry.getMetrics().getNumberOfFailedCallsWithoutRetryAttempt());
        map.put("successfulCallsWithRetryAttempt", retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt());
        return map;
    }

    private Map<String, Object> etatPricing() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("baseUrl", pricingBaseUrl);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = restTemplate.getForObject(pricingBaseUrl + "/api/prices/stats", Map.class);
            map.put("reachable", true);
            map.put("stats", stats);
        } catch (Exception e) {
            map.put("reachable", false);
            map.put("erreur", e.getClass().getSimpleName());
        }
        return map;
    }

    private List<Map<String, Object>> resumeLivres() {
        List<Book> livres = bookRepository.findAll();
        List<Map<String, Object>> liste = new ArrayList<>();
        for (Book livre : livres) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", livre.getId());
            map.put("title", livre.getTitle());
            map.put("author", livre.getAuthor());
            map.put("stock", livre.getStock());
            map.put("version", livre.getVersion());
            liste.add(map);
        }
        return liste;
    }
}
