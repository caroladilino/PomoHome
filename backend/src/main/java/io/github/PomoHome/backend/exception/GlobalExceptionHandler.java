package io.github.PomoHome.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "Recurso não encontrado";
        return ResponseEntity.status(404).body(msg);
    }

    @ExceptionHandler(AutenticacaoException.class)
    public ResponseEntity<String> handleUnauthorized(AutenticacaoException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }
}
