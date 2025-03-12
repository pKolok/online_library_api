package com.mylibrary.onlinelibraryapi;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class OnlineLibraryApiApplication {

    public static void main(String[] args) {
        // Load .env file
        Dotenv dotenv = Dotenv.load();

        // Set system properties
        System.setProperty("OPENAI_API_KEY", Objects.requireNonNull(dotenv.get("OPENAI_API_KEY")));

        SpringApplication.run(OnlineLibraryApiApplication.class, args);
    }

}
