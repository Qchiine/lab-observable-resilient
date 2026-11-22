package com.example.bookservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * Point d'entree metier pour le prix : circuit breaker + fallback.
 *
 * Ordre reel : CircuitBreaker (exterieur) -> Retry (interieur, PricingHttpCaller) -> HTTP
 *  - 3 tentatives HTTP en cas d'echec (retry observable dans les metriques),
 *  - puis 1 echec comptabilise par le circuit breaker,
 *  - puis fallback a 0.0 si le circuit est ouvert / les tentatives epuisees.
 */
@Component
public class PricingClient {

    private static final Logger log = LoggerFactory.getLogger(PricingClient.class);

    private final PricingHttpCaller httpCaller;

    public PricingClient(PricingHttpCaller httpCaller) {
        this.httpCaller = httpCaller;
    }

    @CircuitBreaker(name = "pricing", fallbackMethod = "fallbackPrice")
    public double getPrice(long bookId) {
        return httpCaller.appel(bookId);
    }

    /** Signature imposee par Resilience4j : memes parametres + Throwable. */
    @SuppressWarnings("unused")
    public double fallbackPrice(long bookId, Throwable ex) {
        log.warn("Pricing indisponible pour le livre {} ({}). Fallback = 0.0", bookId, ex.toString());
        return 0.0;
    }
}
