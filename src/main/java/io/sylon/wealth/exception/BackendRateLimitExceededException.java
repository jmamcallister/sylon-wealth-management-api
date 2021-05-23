package io.sylon.wealth.exception;

public class BackendRateLimitExceededException extends RuntimeException {
  public BackendRateLimitExceededException(String message) {
    super(message);
  }
}
