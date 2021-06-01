package io.sylon.wealth.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WatchlistQuotesDto {

  private List<WatchlistItemQuoteDto> quotes;
  private List<WatchlistItemErrorDto> errors;
}
