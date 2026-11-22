package com.example.bookservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookservice.domain.Book;
import com.example.bookservice.repo.BookRepository;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository repo;
    private final PricingClient pricing;

    public BookService(BookRepository repo, PricingClient pricing) {
        this.repo = repo;
        this.pricing = pricing;
    }

    public List<Book> all() {
        return repo.findAll();
    }

    public Book create(Book b) {
        repo.findByTitle(b.getTitle()).ifPresent(existant -> {
            throw new IllegalArgumentException("Titre deja existant");
        });
        b.setStock(Math.max(0, b.getStock()));
        return repo.save(b);
    }

    /**
     * Emprunt : lecture verrouillee + decrement + appel pricing, le tout atomique.
     * Le prix est appele DANS la transaction mais son echec est absorbe par le
     * fallback : on ne veut pas annuler un emprunt valide a cause d'un service externe.
     */
    @Transactional
    public BorrowResult borrow(long id) {
        Book book = repo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable"));

        book.decrementStock();

        double price = pricing.getPrice(id);
        log.info("Emprunt livre {} : stock restant={} prix={}", id, book.getStock(), price);

        return new BorrowResult(book.getId(), book.getTitle(), book.getStock(), price);
    }

    public record BorrowResult(Long id, String title, int stockLeft, double price) {
    }
}
