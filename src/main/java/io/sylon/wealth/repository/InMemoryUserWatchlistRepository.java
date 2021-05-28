package io.sylon.wealth.repository;

import io.sylon.wealth.exception.DuplicateWatchlistNameException;
import io.sylon.wealth.model.core.Watchlist;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserWatchlistRepository implements UserWatchlistRepository {

  private final ConcurrentHashMap<String, List<Watchlist>> userWatchlists = new ConcurrentHashMap<>();

  @Override
  public Watchlist addWatchlist(String user, Watchlist watchlist) {
    List<Watchlist> watchlists = getOrProvisionWatchlists(user);
    if ( watchlists.stream().anyMatch(w -> watchlist.getName().equals(w.getName())) ) {
      throw new DuplicateWatchlistNameException(String.format("Watchlist with name %s already exists", watchlist.getName()));
    }
    Watchlist newWatchlist = newWatchlist(watchlist.getName());
    watchlists.add(newWatchlist(watchlist.getName()));
    return newWatchlist;
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
      watchlists = provisionNewUserWatchlists(user);
    }
    return watchlists;
  }

  private List<Watchlist> provisionNewUserWatchlists(String user) {
    userWatchlists.put(user, newDefaultWatchlist());
    return userWatchlists.get(user);
  }

  private List<Watchlist> newDefaultWatchlist() {
    List<Watchlist> watchlists = new ArrayList<>();
    watchlists.add(newWatchlist("default"));
    return watchlists;
  }

  private Watchlist newWatchlist(String name) {
    return Watchlist.builder().id(UUID.randomUUID().toString()).name(name).watchlistItems(new HashSet<>()).build();
  }
}
