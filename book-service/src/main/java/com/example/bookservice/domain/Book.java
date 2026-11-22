package com.example.bookservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String title;

    @Column(nullable = false, length = 80)
    private String author;

    @Column(nullable = false)
    private int stock;

    /**
     * Verrou optimiste : protege aussi les ecritures concurrentes entre instances.
     * Le verrou pessimiste (findByIdForUpdate) sert pour l'emprunt strict.
     */
    @Version
    private Long version;

    public Book() {
    }

    public Book(String title, String author, int stock) {
        this.title = title;
        this.author = author;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getStock() { return stock; }
    public Long getVersion() { return version; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setStock(int stock) { this.stock = stock; }

    /** Regle metier centralisee : impossible de descendre sous zero. */
    public void decrementStock() {
        if (stock <= 0) {
            throw new IllegalStateException("Plus d'exemplaires");
        }
        stock--;
    }
}
