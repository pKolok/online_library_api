package com.mylibrary.onlinelibraryapi.mockmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylibrary.onlinelibraryapi.model.Book;
import com.mylibrary.onlinelibraryapi.service.BookService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test") // This tells Spring to use application-test.properties
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures database resets after each test
public class BookControllerTests {

    @Autowired
    private BookService bookService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Used to convert Java objects to JSON

    private Book savedBook;

    @BeforeAll
    static void loadEnv() {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("openai.api.key", Objects.requireNonNull(dotenv.get("OPENAI_API_KEY")));
    }

    @BeforeEach
    void setUp() {
        // Save a book in the database before running each test
        savedBook = bookService.createBook(new Book(
                "The Hobbit",
                "J. R. R. Tolkien",
                "9780261103283",
                1937,
                "A fantasy novel about Bilbo Baggins' adventure."
        ));
    }

    @Test
    void testCreateBookHappyDay() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9781234567892", 1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Lord of the Rings: The Return of the King"))
                .andExpect(jsonPath("$.author").value("J. R. R. Tolkien"))
                .andExpect(jsonPath("$.isbn").value("9781234567892"))
                .andExpect(jsonPath("$.publicationYear").value(1955))
                .andExpect(jsonPath("$.description").value("The 3rd part of the series."));
    }

    @Test
    void testCreateBookMissingTitle() throws Exception {
        Book testBook = new Book("", "J. R. R. Tolkien",
                "9781234567892", 1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title cannot be empty"));
    }

    @Test
    void testCreateBookLongTitle() throws Exception {
        String longTitle = "A".repeat(256);
        Book testBook = new Book(longTitle, "J. R. R. Tolkien", "9781234567892", 1955,
                "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title cannot exceed 255 characters"));
    }

    @Test
    void testCreateBookMissingAuthor() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "",
                "9781234567892", 1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.author").value("Author cannot be empty"));
    }

    @Test
    void testCreateBookLongAuthor() throws Exception {
        String longAuthor = "A".repeat(256);
        Book testBook = new Book("The Lord of the Rings: The Return of the King", longAuthor, "9781234567892",
                1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.author").value("Author name cannot exceed 255 characters"));
    }

    @Test
    void testCreateBookMissingIsbn() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                null, 1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").value("ISBN cannot be empty"));
    }

    @Test
    void testCreateBookInvalidIsbn() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "asdsa", 1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").value("Invalid ISBN format"));
    }

    @Test
    void testCreateBookDuplicateIsbn() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9780261103283", 1955, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").value("A book with this ISBN already exists"));
    }

    @Test
    void testCreateBookMissingPublicationYear() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9781234567892", null, "The 3rd part of the series.");

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.publicationYear").value("Publication year cannot be null"));
    }

    @Test
    void testCreateBookEarlyPublicationYear() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9781234567892", 1449, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.publicationYear").value("Publication year must be after 1450"));
    }

    @Test
    void testCreateBookLatePublicationYear() throws Exception {
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9781234567892", 2051, "The 3rd part of the series.");
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.publicationYear").value("Publication year must be before 2050"));
    }

    @Test
    void testCreateBookLongDescription() throws Exception {
        String longDescription = "A".repeat(1001);
        Book testBook = new Book("The Lord of the Rings: The Return of the King", "J. R. R. Tolkien",
                "9781234567892", 2051, longDescription);
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description cannot exceed 1000 characters"));
    }

    @Test
    void testGetAllBooksHappyDay() throws Exception {
        mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value(savedBook.getTitle()))
                .andExpect(jsonPath("$[0].author").value(savedBook.getAuthor()))
                .andExpect(jsonPath("$[0].isbn").value(savedBook.getIsbn()))
                .andExpect(jsonPath("$[0].publicationYear").value(savedBook.getPublicationYear()));
    }

    @Test
    void testGetBookByIdHappyDay() throws Exception {
        mockMvc.perform(get("/books/{id}", savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedBook.getTitle()))
                .andExpect(jsonPath("$.author").value(savedBook.getAuthor()))
                .andExpect(jsonPath("$.isbn").value(savedBook.getIsbn()))
                .andExpect(jsonPath("$.publicationYear").value(savedBook.getPublicationYear()));
    }

    @Test
    void testGetBookByIdNotFound() throws Exception {
        mockMvc.perform(get("/books/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book with id 999 not found"));
    }

    @Test
    void testUpdateBookHappyDay() throws Exception {
        Book updatedBook = new Book(savedBook.getId(), "The Hobbit (Updated)",
                "J. R. R. Tolkien", savedBook.getIsbn(), 1937, "Updated description.");

        mockMvc.perform(put("/books/{id}", savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Hobbit (Updated)"))
                .andExpect(jsonPath("$.description").value("Updated description."));
    }

    @Test
    void testUpdateBookNotFound() throws Exception {
        Book updatedBook = new Book(999L, "Non-existent Book", "Unknown", "1234567890123",
                2000, "No description");

        mockMvc.perform(put("/books/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book with id 999 not found"));
    }

    @Test
    void testUpdateBookDuplicateISBN() throws Exception {
        // Create one more book
        Book savedBook2 = bookService.createBook(new Book(
                "The Silmarillion",
                "J. R. R. Tolkien",
                "9780261103284",
                1977,
                "A collection of myths and stories."
        ));

        // Try to update a book's ISBN to an ISBN which already exists
        Book updatedBook = new Book(savedBook.getId(), "The Hobbit", "J. R. R. Tolkien",
                "9780261103284", 1937, "A fantasy novel about Bilbo Baggins' adventure..");

        mockMvc.perform(put("/books/{id}", savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").value("A book with this ISBN already exists"));
    }

    @Test
    void testDeleteBookHappyDay() throws Exception {
        mockMvc.perform(delete("/books/{id}", savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify the book no longer exists
        mockMvc.perform(get("/books/{id}", savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBookNotFound() throws Exception {
        mockMvc.perform(delete("/books/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book with id 999 not found"));
    }

    @Test
    void testSearchBooksByTitleHappyDay() throws Exception {
        mockMvc.perform(get("/books/search")
                        .param("title", "Hobbit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"));
    }

    @Test
    void testSearchBooksByAuthorHappyDay() throws Exception {
        mockMvc.perform(get("/books/search")
                        .param("author", "Tolkien")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].author").value("J. R. R. Tolkien"));
    }

    @Test
    void testSearchBooksNotFound() throws Exception {
        mockMvc.perform(get("/books/search")
                        .param("title", "Nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

}