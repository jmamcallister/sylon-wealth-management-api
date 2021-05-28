package io.sylon.wealth.repository;

import io.sylon.wealth.model.core.Watchlist;

import java.util.List;

public interface UserWatchlistRepository {
  void addWatchlist(String user, String watchlistName);
  void addSymbolToWatchlist(String user, String watchlistName, String symbol);
  void removeWatchlist(String user, String watchlistName);
  void removeSymbolFromWatchlist(String user, String watchlistName, String symbol);
  List<Watchlist> getWatchlists(String user);
  Watchlist getWatchlistByName(String user, String watchlistName);
}
