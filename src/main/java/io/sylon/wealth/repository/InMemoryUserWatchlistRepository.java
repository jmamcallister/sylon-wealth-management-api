package io.sylon.wealth.repository;

import io.sylon.wealth.exception.DuplicateWatchlistNameException;
import io.sylon.wealth.model.core.Watchlist;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserWatchlistRepository implements UserWatchlistRepository {

  private final ConcurrentHashMap<String, List<Watchlist>> userWatchlists = new ConcurrentHashMap<>();

  @Override
  public void addWatchlist(String user, String watchlistName) {
    List<Watchlist> watchlists = getOrProvisionWatchlists(user);
    if ( watchlists.stream().anyMatch(watchlist -> watchlistName.equals(watchlist.getName())) ) {
      throw new DuplicateWatchlistNameException(String.format("Watchlist with name %s already exists", watchlistName));
    }
    watchlists.add(newWatchlist(watchlistName));
  }

  @Override
  public void addSymbolToWatchlist(String user, String watchlistName, String symbol) {
  }

  @Override
  public void removeWatchlist(String user, String watchlistName) {

  }

  @Override
  public void removeSymbolFromWatchlist(String user, String watchlistName, String symbol) {

  }

  @Override
  public List<Watchlist> getWatchlists(String user) {
    return getOrProvisionWatchlists(user);
  }

  @Override
  public Watchlist getWatchlistByName(String user, String watchlistName) {
    return null;
  }

  private List<Watchlist> getOrProvisionWatchlists(String user) {
    List<Watchlist> watchlists = userWatchlists.get(user);
    if (watchlists == null || watchlists.isEmpty()) {
      provisionNewUserWatchlists(user);
    }
    return watchlists;
  }

  private void provisionNewUserWatchlists(String user) {
    userWatchlists.put(user, newDefaultWatchlist());
  }

  private List<Watchlist> newDefaultWatchlist() {
    List<Watchlist> watchlists = new ArrayList<>();
    watchlists.add(newWatchlist("default"));
    return watchlists;
  }

  private Watchlist newWatchlist(String name) {
    return Watchlist.builder().name(name).watchlistItems(new HashSet<>()).build();
  }
}
