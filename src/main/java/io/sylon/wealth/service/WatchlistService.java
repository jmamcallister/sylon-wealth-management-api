package io.sylon.wealth.service;

import io.sylon.wealth.model.core.Watchlist;
import io.sylon.wealth.model.dto.CreateWatchlistDto;
import io.sylon.wealth.model.dto.CreateWatchlistResponse;
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
    List<WatchlistDto> watchlists = mapEntityToDto(repository.getWatchlists(user));
    return WatchlistsResponse.builder()
        .total(watchlists.size())
        .watchlists(watchlists)
        .build();
  }

  public CreateWatchlistResponse createWatchlist(String user, CreateWatchlistDto createWatchlistDto) {
    Watchlist newWatchlist = repository.addWatchlist(user, mapDtoToEntity(createWatchlistDto));
    return CreateWatchlistResponse.builder().id(newWatchlist.getId()).build();
  }

  private List<WatchlistDto> mapEntityToDto(List<Watchlist> watchlists) {
    return watchlists.stream()
        .map(watchlist -> WatchlistDto.builder()
            .id(watchlist.getId())
            .name(watchlist.getName()).build())
        .collect(Collectors.toList());
  }

  private Watchlist mapDtoToEntity(CreateWatchlistDto createWatchlistDto) {
    return Watchlist.builder().name(createWatchlistDto.getName()).build();
  }
}
