package com.example.bookservice.web;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prouve le multi-instances : chaque conteneur repond avec son hostname Docker
 * et son port INTERNE (toujours 8081, malgre les ports 8081/8083/8084 cote machine).
 */
@RestController
public class InstanceController {

    private final AtomicInteger hits = new AtomicInteger();

    @Value("${server.port}")
    private int port;

    @Value("${spring.profiles.active:default}")
    private String profil;

    @GetMapping("/api/debug/instance")
    public Map<String, Object> instance() {
        String host = System.getenv().getOrDefault("HOSTNAME", "local");

        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("instance", host);
        reponse.put("internalPort", port);
        reponse.put("profile", profil);
        reponse.put("hits", hits.incrementAndGet());
        reponse.put("uptimeSeconds",
                Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()).toSeconds());
        reponse.put("checkedAt", OffsetDateTime.now().toString());
        return reponse;
    }
}
