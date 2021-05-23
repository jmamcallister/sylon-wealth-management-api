package io.sylon.wealth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sylon.wealth.exception.BackendErrorMessageException;
import io.sylon.wealth.exception.BackendInformationException;
import io.sylon.wealth.exception.BackendRateLimitExceededException;
import io.sylon.wealth.exception.BackendUnexpectedException;
import io.sylon.wealth.model.dto.BackendErrorMessageResponse;
import io.sylon.wealth.model.dto.BackendInformationResponse;
import io.sylon.wealth.model.dto.BackendMatch;
import io.sylon.wealth.model.dto.BackendNoteResponse;
import io.sylon.wealth.model.dto.BackendSearchResponse;
import io.sylon.wealth.model.dto.SearchResponse;
import io.sylon.wealth.model.dto.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AlphaVantageSearchService implements SearchService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient webClient;

  @Autowired
  public AlphaVantageSearchService(WebClient webClient) {
    this.webClient = webClient;
  }

  public SearchResponse search(String query) {
    String response = webClient.get().uri(uriBuilder ->
        uriBuilder.queryParam("function", "SYMBOL_SEARCH")
        .queryParam("keywords", query)
        .build())
        .retrieve()
        .bodyToMono(String.class)
        .block();

    checkForNoteResponse(response);
    checkForErrorMessageResponse(response);
    checkForInformationResponse(response);

    return mapResponse(response);
  }

  /*
   * Assumption here is that a response with JSON key 'Note'
   * means the rate limit has been exceeded for the backend
   * API service level
   */
  private void checkForNoteResponse(String response) {
    try {
      BackendNoteResponse noteResponse = objectMapper.readValue(response, BackendNoteResponse.class);
      throw new BackendRateLimitExceededException(noteResponse.getNote());
    } catch (JsonProcessingException e) {
      log.debug("JSON key 'Note' not found in response");
    }
  }

  private void checkForErrorMessageResponse(String response) {
    try {
      BackendErrorMessageResponse errorMessageResponse = objectMapper.readValue(response, BackendErrorMessageResponse.class);
      throw new BackendErrorMessageException(errorMessageResponse.getErrorMessage());
    } catch (JsonProcessingException e) {
      log.debug("JSON key 'Error Message' not found in response");
    }
  }

  private void checkForInformationResponse(String response) {
    try {
      BackendInformationResponse informationResponse = objectMapper.readValue(response, BackendInformationResponse.class);
      throw new BackendInformationException(informationResponse.getInformation());
    } catch (JsonProcessingException e) {
      log.debug("JSON key 'Information' not found in response");
    }
  }

  private SearchResponse mapResponse(String response) {
    BackendSearchResponse backendSearchResponse;

    try {
      backendSearchResponse = objectMapper.readValue(response, BackendSearchResponse.class);
    } catch (JsonProcessingException e) {
      log.error("Unexpected JSON in backend response - {}", response);
      throw new BackendUnexpectedException(response);
    }

    List<SearchResult> searchResults = new ArrayList<>();
    for (BackendMatch match : backendSearchResponse.getBestMatches()) {
      searchResults.add(SearchResult.builder()
          .symbol(match.getSymbol())
          .name(match.getName())
          .build());
    }
    return SearchResponse.builder()
        .total(backendSearchResponse.getBestMatches().size())
        .results(searchResults)
        .build();
  }
}
