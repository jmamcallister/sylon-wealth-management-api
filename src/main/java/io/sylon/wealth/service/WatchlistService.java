package io.sylon.wealth.service;

import io.sylon.wealth.exception.WatchlistSymbolNotFoundException;
import io.sylon.wealth.model.core.Watchlist;
import io.sylon.wealth.model.dto.CreateWatchlistDto;
import io.sylon.wealth.model.dto.CreateWatchlistResponse;
import io.sylon.wealth.model.dto.UpdateWatchlistDto;
import io.sylon.wealth.model.dto.WatchlistDetailResponse;
import io.sylon.wealth.model.dto.WatchlistDto;
import io.sylon.wealth.model.dto.WatchlistsResponse;
import io.sylon.wealth.repository.UserWatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  public WatchlistDetailResponse getWatchlistById(String user, String id) {
    return mapEntityToDto(repository.getWatchlistById(user, id));
  }

  public void updateWatchlistById(String user, String id, UpdateWatchlistDto updateWatchlistDto) {
    Watchlist watchlist = repository.getWatchlistById(user, id);
    if (StringUtils.hasText(updateWatchlistDto.getName())) {
      watchlist.setName(updateWatchlistDto.getName());
    }
    if (!ObjectUtils.isEmpty(updateWatchlistDto.getSymbols())) {
      watchlist.getWatchlistItems().addAll(updateWatchlistDto.getSymbols());
    }
  }

  public void deleteWatchlistById(String user, String id) {
    repository.removeWatchlist(user, id);
  }

  public void deleteSymbolFromWatchlist(String user, String id, String symbol) {
    Watchlist watchlist = repository.getWatchlistById(user, id);
    if (!watchlist.getWatchlistItems().remove(symbol)) {
      throw new WatchlistSymbolNotFoundException(String.format("No symbol %s found in watchlist id %s", symbol, id));
    }
  }

  private WatchlistDetailResponse mapEntityToDto(Watchlist watchlist) {
    return WatchlistDetailResponse.builder()
        .id(watchlist.getId())
        .name(watchlist.getName())
        .symbols(new ArrayList<>(watchlist.getWatchlistItems()))
        .build();
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

  private Optional<List<String>> getOptionalList(List<String> list) {
    if (list == null || list.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(list);
  }
}
