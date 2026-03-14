package com.yaxcherg.smartsupportdashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Este método atrapa las excepciones de validación (cuando falla @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Recorremos todos los errores y armamos un mapa con "campo" : "mensaje de error"
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Devolvemos el mapa de errores con un estado HTTP 400 (Bad Request)
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Aquí podrías agregar más métodos @ExceptionHandler en el futuro para otros tipos de errores
}