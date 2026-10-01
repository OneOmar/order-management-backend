package com.ecommerce.order_management.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions.
 * Transforme les exceptions métier / de validation en réponses JSON structurées.
 * Gère (exemples) :
 * - NotFoundException -> 404
 * - InsufficientStockException -> 400
 * - MethodArgumentNotValidException -> 400 (validation @Valid)
 * - ConstraintViolationException -> 400 (validation de paramètres)
 * - AccessDeniedException -> 403
 * - UsernameNotFoundException -> 401
 * - Exception -> 500 (fallback)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /* Helper : construit un ApiError simple et typé */
  private ApiError buildApiError(HttpStatus status, String message, String path) {
    return new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            null
    );
  }

  private ApiError buildApiError(
          HttpStatus status,
          String message,
          String path,
          Map<String, String> errors
  ) {
    return new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            errors
    );
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
    ApiError body = buildApiError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(InsufficientStockException.class)
  public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
    ApiError body = buildApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                    fe -> fe.getField(),
                    fe -> fe.getDefaultMessage(),
                    (msg1, msg2) -> msg1
            ));

    ApiError body = buildApiError(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            request.getRequestURI(),
            errors
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {

    Map<String, String> errors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (msg1, msg2) -> msg1
            ));

    ApiError body = buildApiError(
            HttpStatus.BAD_REQUEST,
            "Constraint violations",
            request.getRequestURI(),
            errors
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(
          IllegalArgumentException ex,
          HttpServletRequest request
  ) {

    ApiError body = buildApiError(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    ApiError body = buildApiError(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiError> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
    ApiError body = buildApiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ApiError> handleInvalidToken(
          InvalidTokenException ex,
          HttpServletRequest request
  ) {
    ApiError body = buildApiError(
            HttpStatus.UNAUTHORIZED,
            ex.getMessage(),
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ApiError> handleOptimisticLock(
          ObjectOptimisticLockingFailureException ex,
          HttpServletRequest request
  ) {

    ApiError body = buildApiError(
            HttpStatus.CONFLICT,
            "Conflit de mise à jour : la ressource a été modifiée par un autre utilisateur",
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {

    ex.printStackTrace();

    ApiError body = buildApiError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getMessage(),
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}