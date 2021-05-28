package io.sylon.wealth.model.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Watchlist {
  private long id;
  private String name;
  private Set<String> watchlistItems;
}
