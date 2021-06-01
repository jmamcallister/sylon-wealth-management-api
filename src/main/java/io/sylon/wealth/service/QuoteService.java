package io.sylon.wealth.service;

import io.sylon.wealth.model.backend.QuoteResponse;

import java.util.List;

public interface QuoteService {

  List<QuoteResponse> getQuotes(List<String> symbols);
}
