package com.mylibrary.onlinelibraryapi.testresttemplate.controller;

import com.mylibrary.onlinelibraryapi.model.Book;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("openai.api.key", Objects.requireNonNull(dotenv.get("OPENAI_API_KEY")));
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/books";

        // Print current database contents
//        ResponseEntity<List<Book>> response = restTemplate.exchange(
//                baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
//        if (response.getBody() != null && !response.getBody().isEmpty()) {
//            System.out.println("Current Database State:");
//            response.getBody().forEach(book -> System.out.println(book.toString()));
//        }
    }

    @Test
    void testCreateBookHappyDay() {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9781234567892", 1955, "The 3rd part of the series.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("title")).isEqualTo("The Lord of the Rings: The Return of the King");
        assertThat(response.getBody().get("author")).isEqualTo("J. R. R. Tolkien");
        assertThat(response.getBody().get("isbn")).isEqualTo("9781234567892");
        assertThat(response.getBody().get("publicationYear")).isEqualTo("1955");
        assertThat(response.getBody().get("description")).isEqualTo("The 3rd part of the series.");
    }

    @Test
    void testCreateBookMissingTitle() {
        Book testBook = new Book("", "J. R. R. Tolkien", "9781234567892", 1955,
                "The 3rd part of the series.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("title")).isEqualTo("Title cannot be empty");
    }

    @Test
    void testCreateBookLongTitle() {
        String longTitle = "A".repeat(256);
        Book testBook = new Book(longTitle, "J. R. R. Tolkien", "9781234567892", 1955,
                "The 3rd part of the series.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("title")).isEqualTo("Title cannot exceed 255 characters");
    }

    @Test
    void testCreateBookMissingAuthor() {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "", "9781234567892",
                1955, "The 3rd part of the series.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("author")).isEqualTo("Author cannot be empty");
    }

    @Test
    void testCreateBookLongAuthor() {
        String longAuthor = "A".repeat(256);
        Book testBook = new Book("The Lord of the Rings: The Return of the King", longAuthor, "9781234567892",
                1955, "The 3rd part of the series.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("author")).isEqualTo("Author name cannot exceed 255 characters");
    }

    @Test
    void testCreateBookMissingIsbn() {
        Book testBook = new Book("The Lord of the Rings", "J. R. R. Tolkien", null, 1955,
                "A book.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("isbn")).isEqualTo("ISBN cannot be empty");
    }

    @Test
    void testCreateBookInvalidIsbn() {
        Book testBook = new Book("The Lord of the Rings", "J. R. R. Tolkien", "123", 1955,
                "A book.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("isbn")).isEqualTo("Invalid ISBN format");
    }

    @Test
    void testCreateBookDuplicateIsbn() {
        Book book1 = new Book("1984", "George Orwell", "9780451524935", 1949,
                "Dystopian novel.");
        ResponseEntity<Book> createResponse = restTemplate.postForEntity(baseUrl, book1, Book.class);
        Long book1Id = Objects.requireNonNull(createResponse.getBody()).getId();

        Book book2 = new Book("The Lord of the Rings", "J. R. R. Tolkien", "9780451524935",
                1955, "A book.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(book2), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("isbn")).isEqualTo("A book with this ISBN already exists");

        restTemplate.delete(baseUrl + "/" + book1Id);
    }

    @Test
    void testCreateBookMissingPublicationYear() {
        Book testBook = new Book("The Lord of the Rings", "J. R. R. Tolkien", "9781234567892",
                null, "A book.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("publicationYear")).isEqualTo("Publication year cannot be null");
    }

    @Test
    void testCreateBookEarlyPublicationYear() {
        Book testBook = new Book("The Lord of the Rings", "J. R. R. Tolkien", "9781234567892",
                1400, "A book.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("publicationYear")).isEqualTo("Publication year must be after 1450");
    }

    @Test
    void testCreateBookLatePublicationYear() {
        Book testBook = new Book("The Lord of the Rings", "J. R. R. Tolkien", "9781234567892",
                2100, "A book.");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("publicationYear")).isEqualTo("Publication year must be before 2050");
    }

    @Test
    void testCreateBookLongDescription() {
        String longDescription = "A".repeat(1001);
        Book testBook = new Book("The Lord of the Rings", "J. R. R. Tolkien", "9781234567892",
                1955, longDescription);

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl, HttpMethod.POST,
                new HttpEntity<>(testBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("description")).isEqualTo("Description cannot exceed 1000 characters");
    }

    @Test
    void testGetAllBooksHappyDay() {
        // First, create a couple of books
        Book testBook1 = new Book("1984", "George Orwell", "9780451524935", 1949,
                "Dystopian novel.");
        Book testBook2 = new Book("Brave New World", "Aldous Huxley", "9780060850524",
                1932, "Dystopian novel.");

        // Create the books using postForEntity
        restTemplate.postForEntity(baseUrl, testBook1, Book.class);
        restTemplate.postForEntity(baseUrl, testBook2, Book.class);

        // Now, retrieve all books
        ResponseEntity<List<Book>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });

        // Assert that the response status is OK and the list is not empty
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);

        // Optionally, you can validate specific book details to ensure they were correctly added
        List<Book> books = response.getBody();
        assertThat(books.stream().anyMatch(book -> book.getTitle().equals("1984"))).isTrue();
        assertThat(books.stream().anyMatch(book -> book.getTitle().equals("Brave New World"))).isTrue();

        // Clean up (delete the books created for the test)
        books.forEach(book -> restTemplate.delete(baseUrl + "/" + book.getId()));
    }

    @Test
    void testGetBookByIdHappyDay() {
        // First, create a book
        Book testBook = new Book("1984", "George Orwell", "9780451524935", 1949,
                "Dystopian novel.");
        ResponseEntity<Book> createResponse = restTemplate.postForEntity(baseUrl, testBook, Book.class);
        Long bookId = Objects.requireNonNull(createResponse.getBody()).getId();

        // Now, retrieve the book
        ResponseEntity<Book> response = restTemplate.getForEntity(baseUrl + "/" + bookId, Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("1984");

        restTemplate.delete(baseUrl + "/" + bookId);
    }

    @Test
    void testGetBookByIdNotFound() {
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl + "/999",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Book with id 999 not found");
    }

    @Test
    void testUpdateBookHappyDay() {
        // First, create a book
        Book testBook = new Book("1984", "George Orwell", "9780451524935", 1949,
                "Dystopian novel.");
        ResponseEntity<Book> createResponse = restTemplate.postForEntity(baseUrl, testBook, Book.class);
        Long bookId = Objects.requireNonNull(createResponse.getBody()).getId();

        // Updated book details
        Book updatedBook = new Book(bookId, "1984 (Updated)", "George Orwell", "9780451524935",
                1950, "Updated description.");

        ResponseEntity<Book> response = restTemplate.exchange(baseUrl + "/" + bookId, HttpMethod.PUT,
                new HttpEntity<>(updatedBook), Book.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("1984 (Updated)");
        assertThat(response.getBody().getPublicationYear()).isEqualTo(1950);

        restTemplate.delete(baseUrl + "/" + bookId);
    }

    @Test
    void testUpdateBookNotFound() {
        Book updatedBook = new Book(999L, "Non-existent Book", "Unknown", "1234567890123",
                2000, "No description");

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl + "/999", HttpMethod.PUT,
                new HttpEntity<>(updatedBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Book with id 999 not found");
    }

    @Test
    void testUpdateBookDuplicateISBN() {
        // First, create 2 books
        Book book1 = new Book("1984", "George Orwell", "9780451524935", 1949,
                "Dystopian novel.");
        ResponseEntity<Book> createResponse1 = restTemplate.postForEntity(baseUrl, book1, Book.class);
        Long book1Id = Objects.requireNonNull(createResponse1.getBody()).getId();

        Book book2 = new Book("The Hobbit", "J. R. R. Tolkien", "9780345339683", 1937,
                "Fantasy novel");
        ResponseEntity<Book> createResponse2 = restTemplate.postForEntity(baseUrl, book2, Book.class);
        Long book2Id = Objects.requireNonNull(createResponse2.getBody()).getId();

        // Updated book details
        Book updatedBook = new Book(book1Id, "1984 (Updated)", "George Orwell", "9780345339683",
                1949, "Updated description.");

        // Updated book details
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl + "/" + book1Id,
                HttpMethod.PUT, new HttpEntity<>(updatedBook), new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("isbn")).isEqualTo("A book with this ISBN already exists");

        restTemplate.delete(baseUrl + "/" + book1Id);
        restTemplate.delete(baseUrl + "/" + book2Id);
    }

    @Test
    void testDeleteBookHappyDay() {
        // First, create a book
        Book testBook = new Book("1984", "George Orwell", "9780451524935", 1949,
                "Dystopian novel.");
        ResponseEntity<Book> createResponse = restTemplate.postForEntity(baseUrl, testBook, Book.class);
        Long bookId = Objects.requireNonNull(createResponse.getBody()).getId();

        // Delete the book
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(baseUrl + "/" + bookId, HttpMethod.DELETE,
                null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify book is deleted
        ResponseEntity<Map<String, String>> getResponse = restTemplate.exchange(baseUrl + "/" + bookId,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteBookNotFound() {
        ResponseEntity<Map<String, String>> response = restTemplate.exchange(baseUrl + "/999", HttpMethod.DELETE,
                null, new ParameterizedTypeReference<>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Book with id 999 not found");
    }

    @Test
    void testSearchBooksByTitleHappyDay() {
        // Create the books first
        Book testBook1 = new Book("The Hobbit", "J. R. R. Tolkien", "9780345339683", 1937,
                "Fantasy novel");
        restTemplate.postForEntity(baseUrl, testBook1, Book.class);

        // Perform the search request
        ResponseEntity<List<Book>> response = restTemplate.exchange(
                baseUrl + "/search?title=Hobbit", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        // Assert the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1); // Expect 1 book to be found
        assertThat(response.getBody().get(0).getTitle()).isEqualTo("The Hobbit");

        // Clean up (delete the created book)
        restTemplate.delete(baseUrl + "/" + response.getBody().get(0).getId());
    }

    @Test
    void testSearchBooksByAuthorHappyDay() {
        // Create the books first
        Book testBook2 = new Book("The Hobbit", "J. R. R. Tolkien", "9780345339683", 1937, "Fantasy novel");
        restTemplate.postForEntity(baseUrl, testBook2, Book.class);

        // Perform the search request
        ResponseEntity<List<Book>> response = restTemplate.exchange(
                baseUrl + "/search?author=Tolkien", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        // Assert the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1); // Expect 1 book to be found
        assertThat(response.getBody().get(0).getAuthor()).isEqualTo("J. R. R. Tolkien");

        // Clean up (delete the created book)
        restTemplate.delete(baseUrl + "/" + response.getBody().get(0).getId());
    }

    @Test
    void testSearchBooksNotFound() {
        // Perform the search request for a title that doesn't exist
        ResponseEntity<List<Book>> response = restTemplate.exchange(
                baseUrl + "/search?title=Nonexistent", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {});

        // Assert the response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(0); // No books should be found
    }

}