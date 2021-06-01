package io.sylon.wealth.model.backend.alphavantage;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackendQuoteResponse {

  @JsonProperty("Global Quote")
  private GlobalQuote globalQuote;
}
