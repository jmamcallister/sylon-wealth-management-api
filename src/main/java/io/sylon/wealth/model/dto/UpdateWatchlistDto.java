package io.sylon.wealth.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sylon.wealth.validator.AnyNotNull;
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
@AnyNotNull
public class UpdateWatchlistDto {
  private String name;
  private List<String> symbols;
}
