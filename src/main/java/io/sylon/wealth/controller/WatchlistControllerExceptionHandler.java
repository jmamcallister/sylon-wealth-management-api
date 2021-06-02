package io.sylon.wealth.controller;

import io.sylon.wealth.exception.DuplicateWatchlistNameException;
import io.sylon.wealth.exception.UnknownServerErrorException;
import io.sylon.wealth.exception.WatchlistNotFoundException;
import io.sylon.wealth.exception.WatchlistSymbolNotFoundException;
import io.sylon.wealth.model.backend.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@SuppressWarnings ("rawtypes")
public class WatchlistControllerExceptionHandler {

  @ResponseStatus (HttpStatus.BAD_REQUEST)
  @ExceptionHandler (MethodArgumentNotValidException.class)
  public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error(e.getBindingResult().toString());
    return ErrorResponse.builder()
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .errorMessage(e.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(",")))
        .build();
  }

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
