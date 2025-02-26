package com.mylibrary.onlinelibraryapi.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class CustomException extends RuntimeException {
    private final Map<String, String> errors;

    public CustomException(Map<String, String> errors) {
        super("Validation failed");
        this.errors = errors;
    }

}