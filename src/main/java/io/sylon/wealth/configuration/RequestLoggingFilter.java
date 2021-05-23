package io.sylon.wealth.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class RequestLoggingFilter {

  /*
   * Just the URL and method, can log more if required
   */
  public static ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      log.info("{} {}", clientRequest.method().name(), clientRequest.url());
      return Mono.just(clientRequest);
    });
  }
}
