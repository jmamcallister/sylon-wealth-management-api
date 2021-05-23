package io.sylon.wealth.service;

import io.sylon.wealth.model.dto.SearchResponse;

public interface SearchService {

  SearchResponse search(String query);
}
