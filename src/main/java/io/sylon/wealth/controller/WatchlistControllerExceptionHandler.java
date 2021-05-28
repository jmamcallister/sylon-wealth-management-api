package io.sylon.wealth.controller;

import io.sylon.wealth.exception.DuplicateWatchlistNameException;
import io.sylon.wealth.model.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
@SuppressWarnings ("rawtypes")
public class WatchlistControllerExceptionHandler {

  @ResponseStatus (HttpStatus.CONFLICT)
  @ExceptionHandler (DuplicateWatchlistNameException.class)
  public ErrorResponse handleDuplicateResourceException(Exception e) {
    log.error(e.getMessage());
    return ErrorResponse.builder()
        .error(HttpStatus.CONFLICT.getReasonPhrase())
        .errorMessage(Optional.ofNullable(e.getMessage()).orElse("Resource already exists"))
        .build();
  }
}
