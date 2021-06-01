package io.sylon.wealth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sylon.wealth.exception.BackendErrorMessageException;
import io.sylon.wealth.exception.BackendInformationException;
import io.sylon.wealth.exception.BackendRateLimitExceededException;
import io.sylon.wealth.model.dto.BackendErrorMessageResponse;
import io.sylon.wealth.model.dto.BackendInformationResponse;
import io.sylon.wealth.model.dto.BackendNoteResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAlphaVantageService {

  protected final ObjectMapper objectMapper = new ObjectMapper();

  /*
   * Assumption here is that a response with JSON key 'Note'
   * means the rate limit has been exceeded for the backend
   * API service level
   */
  protected void checkForNoteResponse(String response) {
    try {
      BackendNoteResponse noteResponse = objectMapper.readValue(response, BackendNoteResponse.class);
      throw new BackendRateLimitExceededException(noteResponse.getNote());
    } catch (JsonProcessingException e) {
      log.debug("JSON key 'Note' not found in response");
    }
  }

  protected void checkForErrorMessageResponse(String response) {
    try {
      BackendErrorMessageResponse errorMessageResponse = objectMapper.readValue(response, BackendErrorMessageResponse.class);
      throw new BackendErrorMessageException(errorMessageResponse.getErrorMessage());
    } catch (JsonProcessingException e) {
      log.debug("JSON key 'Error Message' not found in response");
    }
  }

  protected void checkForInformationResponse(String response) {
    try {
      BackendInformationResponse informationResponse = objectMapper.readValue(response, BackendInformationResponse.class);
      throw new BackendInformationException(informationResponse.getInformation());
    } catch (JsonProcessingException e) {
      log.debug("JSON key 'Information' not found in response");
    }
  }
}
