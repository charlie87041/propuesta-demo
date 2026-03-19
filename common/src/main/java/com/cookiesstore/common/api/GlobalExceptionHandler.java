package com.cookiesstore.common.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldValidationError> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toValidationError)
            .toList();

        return ResponseEntity.badRequest().body(ApiResponse.validationError("Validation failed", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldValidationError> details = ex.getConstraintViolations()
            .stream()
            .map(this::toValidationError)
            .toList();

        return ResponseEntity.badRequest().body(ApiResponse.validationError("Validation failed", details));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionValidation(TransactionSystemException ex) {
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof ConstraintViolationException validationEx) {
            return handleConstraintViolation(validationEx);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred: " + ex.getMessage()));
    }

    private FieldValidationError toValidationError(FieldError fieldError) {
        return new FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private FieldValidationError toValidationError(ConstraintViolation<?> violation) {
        return new FieldValidationError(violation.getPropertyPath().toString(), violation.getMessage());
    }
}
