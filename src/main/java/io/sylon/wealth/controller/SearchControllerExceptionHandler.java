package io.sylon.wealth.controller;

import io.sylon.wealth.exception.BackendErrorMessageException;
import io.sylon.wealth.exception.BackendRateLimitExceededException;
import io.sylon.wealth.model.backend.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
@SuppressWarnings ("rawtypes")
public class SearchControllerExceptionHandler {

  @ResponseStatus (HttpStatus.TOO_MANY_REQUESTS)
  @ExceptionHandler (BackendRateLimitExceededException.class)
  public ErrorResponse handleRateLimitExceededException(Exception e) {
    log.error(e.getMessage());
    return ErrorResponse.builder()
        .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
        .errorMessage(Optional.ofNullable(e.getMessage()).orElse("Rate limit exceeded"))
        .build();
  }

  @ResponseStatus (HttpStatus.BAD_REQUEST)
  @ExceptionHandler (BackendErrorMessageException.class)
  public ErrorResponse handleErrorMessageException(Exception e) {
    log.error(e.getMessage());
    return ErrorResponse.builder()
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .errorMessage(Optional.ofNullable(e.getMessage()).orElse("No message available"))
        .build();
  }
}
