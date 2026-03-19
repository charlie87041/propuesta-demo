package com.cookiesstore.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void constraintViolationExceptionReturnsValidationErrorResponse() {
        Set<jakarta.validation.ConstraintViolation<ValidationBean>> violations =
            VALIDATOR.validate(new ValidationBean(""));
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().error().details()).isNotEmpty();
        assertThat(response.getBody().error().details().get(0).field()).isEqualTo("name");
    }

    record ValidationBean(@NotBlank String name) {
    }
}
