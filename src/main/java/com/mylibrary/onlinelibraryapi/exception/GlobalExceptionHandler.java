package com.mylibrary.onlinelibraryapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles @Valid validation errors (e.g., @NotBlank, @Size)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    // Handles non existing validation errors (e.g., id does not exist)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFieldValidationExceptions(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getErrors());
    }

    // Handles custom validation errors (e.g., duplicate ISBN)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleFieldValidationExceptions(CustomException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrors());
    }

}