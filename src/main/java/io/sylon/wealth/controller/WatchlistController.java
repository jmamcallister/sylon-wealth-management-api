package io.sylon.wealth.controller;

import io.sylon.wealth.model.dto.CreateWatchlistDto;
import io.sylon.wealth.model.dto.CreateWatchlistResponse;
import io.sylon.wealth.model.dto.WatchlistsResponse;
import io.sylon.wealth.service.WatchlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@Slf4j
public class WatchlistController {

  private final WatchlistService watchlistService;

  @Autowired
  public WatchlistController(WatchlistService watchlistService) {
    this.watchlistService = watchlistService;
  }

  @GetMapping ("/watchlists")
  public WatchlistsResponse getWatchlists(Principal principal) {
    return watchlistService.getWatchlists(principal.getName());
  }

  @PostMapping ("/watchlists")
  @ResponseStatus (HttpStatus.CREATED)
  public CreateWatchlistResponse createWatchlist(@RequestBody CreateWatchlistDto createWatchlistDto, Principal principal) {
    log.debug("User: {}", principal.getName());
    return watchlistService.createWatchlist(principal.getName(), createWatchlistDto);
  }
}
