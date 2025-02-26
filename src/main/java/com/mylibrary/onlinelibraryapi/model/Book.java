package com.mylibrary.onlinelibraryapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated unique ID
    private Long id;

    @Column(nullable = false) // nullable concerns the db not the api
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Column(nullable = false) // Author is required
    @NotBlank(message = "Author cannot be empty")
    @Size(max = 255, message = "Author name cannot exceed 255 characters")
    private String author;

    @Column(unique = true, nullable = false) // ISBN should be unique and required
    @NotBlank(message = "ISBN cannot be empty")
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "Invalid ISBN format")
    private String isbn;

    @Column(nullable = false)
    @Min(value = 1450, message = "Publication year must be after 1450")
    @Max(value = 2050, message = "Publication year must be before 2050")
    private int publicationYear;

    @Lob // Use for long text storage
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}