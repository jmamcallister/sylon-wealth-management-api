package io.sylon.wealth.controller;

import io.sylon.wealth.exception.DuplicateWatchlistNameException;
import io.sylon.wealth.exception.UnknownServerErrorException;
import io.sylon.wealth.exception.WatchlistNotFoundException;
import io.sylon.wealth.exception.WatchlistSymbolNotFoundException;
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

  @ResponseStatus (HttpStatus.NOT_FOUND)
  @ExceptionHandler ({WatchlistNotFoundException.class, WatchlistSymbolNotFoundException.class})
  public ErrorResponse handleWatchlistNotFoundException(Exception e) {
    log.error(e.getMessage());
    return ErrorResponse.builder()
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .errorMessage(Optional.ofNullable(e.getMessage()).orElse("Resource not found"))
        .build();
  }

  @ResponseStatus (HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler (UnknownServerErrorException.class)
  public ErrorResponse handleServerErrorException(Exception e) {
    log.error(e.getMessage());
    return ErrorResponse.builder()
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .errorMessage(Optional.ofNullable(e.getMessage()).orElse("An internal server error occurred"))
        .build();
  }
}
