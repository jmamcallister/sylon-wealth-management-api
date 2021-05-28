package io.sylon.wealth.controller;

import io.sylon.wealth.model.dto.WatchlistsResponse;
import io.sylon.wealth.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
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
}
