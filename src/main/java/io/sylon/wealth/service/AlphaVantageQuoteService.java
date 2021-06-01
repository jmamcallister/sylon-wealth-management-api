package io.sylon.wealth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sylon.wealth.exception.BackendUnexpectedException;
import io.sylon.wealth.exception.UnknownServerErrorException;
import io.sylon.wealth.model.backend.QuoteResponse;
import io.sylon.wealth.model.backend.alphavantage.BackendQuoteResponse;
import io.sylon.wealth.model.backend.alphavantage.GlobalQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlphaVantageQuoteService extends AbstractAlphaVantageService implements QuoteService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient webClient;

  @Autowired
  public AlphaVantageQuoteService(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public List<QuoteResponse> getQuotes(List<String> symbols) {
    List<String> rawQuoteResponses = Flux.fromIterable(symbols)
        .parallel()
        .runOn(Schedulers.boundedElastic())
        .flatMap(this::getQuote)
        .sequential()
        .collectList().block();
    if (rawQuoteResponses == null) {
      throw new UnknownServerErrorException("No response from quote service");
    }
    return rawQuoteResponses.stream()
        .map(response -> {
          checkForNoteResponse(response);
          checkForErrorMessageResponse(response);
          checkForInformationResponse(response);
          return mapResponse(response);
        })
        .collect(Collectors.toList());
  }

  private Mono<String> getQuote(String symbol) {
    return webClient.get().uri(uriBuilder -> uriBuilder
        .queryParam("function", "GLOBAL_QUOTE")
        .queryParam("symbol", symbol)
        .build())
        .retrieve()
        .bodyToMono(String.class);
  }

  private QuoteResponse mapResponse(String response) {

    BackendQuoteResponse backendQuoteResponse;

    try {
      backendQuoteResponse = objectMapper.readValue(response, BackendQuoteResponse.class);
    } catch (JsonProcessingException e) {
      log.error("Unexpected JSON in backend response - {}", response);
      log.error("Exception", e);
      throw new BackendUnexpectedException(response);
    }

    GlobalQuote globalQuote = backendQuoteResponse.getGlobalQuote();

    return QuoteResponse.builder()
        .symbol(globalQuote.getSymbol())
        .price(globalQuote.getPrice())
        .build();
  }
}
