package com.mylibrary.onlinelibraryapi.controller;

import com.mylibrary.onlinelibraryapi.exception.CustomException;
import com.mylibrary.onlinelibraryapi.model.Book;
import com.mylibrary.onlinelibraryapi.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Value("${openai.api.key}") // Inject API Key from properties
    private String openAiApiKey;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // Create a New Book
    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody Book book) {
        if (bookService.doesBookExistByIsbn(book.getIsbn())) {
            throw new CustomException(Map.of("isbn", "A book with this ISBN already exists"));
        }
        return ResponseEntity.ok(bookService.createBook(book));
    }

    // Retrieve All Books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // Retrieve a Single Book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> currentBookOptional = bookService.getBookById(id);
        if (currentBookOptional.isEmpty()) {
            throw new CustomException(Map.of("error", String.format("Book with id %d not found", id)));
        }

        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update an Existing Book
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        Optional<Book> currentBookOptional = bookService.getBookById(id);
        if (currentBookOptional.isEmpty()) {
            throw new CustomException(Map.of("error", String.format("Book with id %d not found", id)));
        }

        Book currentBook = currentBookOptional.get();

        if (!currentBook.getIsbn().equals(updatedBook.getIsbn()) && bookService.doesBookExistByIsbn(updatedBook.getIsbn())) {
            throw new CustomException(Map.of("isbn", "A book with this ISBN already exists"));
        }

        return bookService.updateBook(id, updatedBook)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a Book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        Optional<Book> currentBookOptional = bookService.getBookById(id);
        if (currentBookOptional.isEmpty()) {
            throw new CustomException(Map.of("error", String.format("Book with id %d not found", id)));
        }

        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // Search for Books
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(required = false) String title,
                                                  @RequestParam(required = false) String author) {
        return ResponseEntity.ok(bookService.searchBooks(title, author));
    }

    @GetMapping("/{id}/ai-insights")
    public ResponseEntity<String> getAiInsights(@PathVariable Long id) {
        Optional<Book> bookOptional = bookService.getBookById(id);
        if (bookOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book = bookOptional.get();
        String prompt = "Generate a catchy tagline for this book: " + book.getTitle() + " by " + book.getAuthor()
                + ". Description: " + book.getDescription();

        WebClient webClient = WebClient.create("https://api.openai.com/v1/chat/completions");
        String aiResponse = webClient.post()
                .header("Authorization", "Bearer " + openAiApiKey)
                .bodyValue("{\"model\":\"gpt-4\",\"messages\":[{\"role\":\"user\",\"content\":\"" + prompt + "\"}]}")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return ResponseEntity.ok(aiResponse);
    }
}
