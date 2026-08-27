package com.attendai.ai.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

record ApiError(Instant ts, int status, String error, String msg, String path) {}

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleVal(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fe = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String m = fe == null ? "Validation failed" : fe.getField() + ": " + fe.getDefaultMessage();
        return build(HttpStatus.UNPROCESSABLE_ENTITY, m, req.getRequestURI());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req.getRequestURI());
    }
    private ResponseEntity<ApiError> build(HttpStatus s, String m, String p) {
        return ResponseEntity.status(s).body(new ApiError(Instant.now(), s.value(), s.getReasonPhrase(), m, p));
    }
}
