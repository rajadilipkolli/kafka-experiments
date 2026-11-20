package com.example.springbootkafkasample.config;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ProblemDetail onException(
            MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest request) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request content.");
        problemDetail.setTitle("Constraint Violation");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        List<ApiValidationError> validationErrorsList = methodArgumentNotValidException.getAllErrors().stream()
                .map(objectError -> {
                    FieldError fieldError = (FieldError) objectError;
                    return new ApiValidationError(
                            fieldError.getObjectName(),
                            fieldError.getField(),
                            fieldError.getRejectedValue(),
                            Objects.requireNonNull(fieldError.getDefaultMessage(), ""));
                })
                .sorted(Comparator.comparing(ApiValidationError::field))
                .toList();
        problemDetail.setProperty("violations", validationErrorsList);
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), ex.getMessage());
        problemDetail.setTitle("Bad Request");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleInvalidEnum(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail problemDetail;
        if (ex.getMessage()
                .contains("Cannot deserialize value of type `com.example.springbootkafkasample.dto.Operation`")) {
            problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatusCode.valueOf(400), "Invalid operation value. Allowed values are: START, STOP.");
        } else {
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid request.");
        }
        problemDetail.setTitle("Bad Request");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    /** Returns an HTTP 503 problem detail with the Kafka or timeout failure's message. */
    @ExceptionHandler({
        org.springframework.kafka.KafkaException.class,
        org.apache.kafka.common.KafkaException.class,
        java.util.concurrent.TimeoutException.class
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ProblemDetail handleKafkaException(Exception ex, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(503), ex.getMessage());
        problemDetail.setTitle("Service Unavailable");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    record ApiValidationError(String object, String field, Object rejectedValue, String message) {}
}
