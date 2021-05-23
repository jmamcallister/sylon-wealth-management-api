package io.sylon.wealth.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sylon.wealth.model.dto.BackendMatch;
import io.sylon.wealth.model.dto.BackendNoteResponse;
import io.sylon.wealth.model.dto.BackendSearchResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestData {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String json(Object o) throws JsonProcessingException {
    return objectMapper.writeValueAsString(o);
  }

  public static BackendSearchResponse singleBackendSearchResponse(String symbol, String name) {
    return BackendSearchResponse.builder()
        .bestMatches(Collections.singletonList(BackendMatch.builder().symbol(symbol).name(name).build()))
        .build();
  }

  public static BackendNoteResponse tooManyRequestsResponse() {
    return BackendNoteResponse.builder()
        .note("5 per minute, 500 per day")
        .build();
  }
}
