package io.sylon.wealth.controller;

import io.sylon.wealth.model.dto.SearchResponse;
import io.sylon.wealth.service.SearchService;
import org.intellij.lang.annotations.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

  private final SearchService searchService;

  @Autowired
  public SearchController(SearchService searchService) {
    this.searchService = searchService;
  }

  @GetMapping("/search")
  @Validated
  public ResponseEntity<SearchResponse> search(@RequestParam("query") @Pattern("[a-zA-Z0-9_\\s]{0,20}") String query) {
    return new ResponseEntity<>(searchService.search(query), HttpStatus.OK);
  }
}
