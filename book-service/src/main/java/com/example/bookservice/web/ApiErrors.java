package com.example.bookservice.web;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> badRequest(IllegalArgumentException ex) {
        return corps(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Stock epuise : 409 (conflit) plutot qu'un 500. */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> conflict(IllegalStateException ex) {
        return corps(HttpStatus.CONFLICT, ex.getMessage());
    }

    private Map<String, Object> corps(HttpStatus statut, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", OffsetDateTime.now().toString());
        map.put("status", statut.value());
        map.put("error", message);
        return map;
    }
}
