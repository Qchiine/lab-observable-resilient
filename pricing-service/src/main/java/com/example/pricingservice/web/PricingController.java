package com.example.pricingservice.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de pricing. Deux mecanismes de panne pour rendre Resilience4j observable :
 *  - fail=true  : panne forcee a la demande ;
 *  - mode "chaos" activable a distance, sans redemarrer le conteneur.
 */
@RestController
@RequestMapping("/api/prices")
public class PricingController {

    /** Etat global du chaos, pilotable via /api/chaos. */
    private final AtomicBoolean chaos = new AtomicBoolean(false);
    private final AtomicInteger failurePercent = new AtomicInteger(30);
    private final AtomicInteger appels = new AtomicInteger();
    private final AtomicInteger echecs = new AtomicInteger();

    @Value("${spring.application.name:pricing-service}")
    private String nomService;

    @GetMapping(value = "/{bookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> price(@PathVariable long bookId,
                                     @RequestParam(name = "fail", defaultValue = "false") boolean fail) {
        appels.incrementAndGet();

        if (fail) {
            echecs.incrementAndGet();
            throw new IllegalStateException("Pricing down (forced)");
        }
        if (chaos.get() && ThreadLocalRandom.current().nextInt(100) < failurePercent.get()) {
            echecs.incrementAndGet();
            throw new IllegalStateException("Random failure");
        }

        double prix = 50.0 + (bookId % 10) * 5.0;
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("bookId", bookId);
        reponse.put("price", prix);
        reponse.put("currency", "MAD");
        reponse.put("service", nomService);
        return reponse;
    }

    /**
     * Ancien contrat : certains clients attendent encore un simple nombre.
     * On le conserve sous un chemin dedie pour ne rien casser.
     */
    @GetMapping(value = "/{bookId}/amount", produces = MediaType.APPLICATION_JSON_VALUE)
    public double amount(@PathVariable long bookId,
                         @RequestParam(name = "fail", defaultValue = "false") boolean fail) {
        appels.incrementAndGet();
        if (fail) {
            echecs.incrementAndGet();
            throw new IllegalStateException("Pricing down (forced)");
        }
        return 50.0 + (bookId % 10) * 5.0;
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> stats() {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("service", nomService);
        reponse.put("calls", appels.get());
        reponse.put("failures", echecs.get());
        reponse.put("chaos", chaos.get());
        reponse.put("failurePercent", failurePercent.get());
        return reponse;
    }

    /** Active / desactive la panne aleatoire a distance (defi du lab). */
    @PostMapping(value = "/chaos", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> chaos(@RequestParam(name = "enabled") boolean enabled,
                                     @RequestParam(name = "percent", defaultValue = "30") int percent) {
        chaos.set(enabled);
        failurePercent.set(Math.max(0, Math.min(100, percent)));
        return stats();
    }
}
