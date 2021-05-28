package io.sylon.wealth.service;

import io.sylon.wealth.model.core.Watchlist;
import io.sylon.wealth.model.dto.WatchlistDto;
import io.sylon.wealth.model.dto.WatchlistsResponse;
import io.sylon.wealth.repository.UserWatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WatchlistService {

  private final UserWatchlistRepository repository;

  @Autowired
  public WatchlistService(UserWatchlistRepository repository) {
    this.repository = repository;
  }

  public WatchlistsResponse getWatchlists(String user) {
    List<Watchlist> watchlists = repository.getWatchlists(user);
    return mapEntityToDto(watchlists);
  }

  private WatchlistsResponse mapEntityToDto(List<Watchlist> watchlists) {
    return WatchlistsResponse.builder()
        .watchlists(
            watchlists.stream()
                .map(watchlist -> WatchlistDto.builder()
                    .id(watchlist.getId())
                    .name(watchlist.getName()).build())
                .collect(Collectors.toList())
        )
        .build();
  }
}
