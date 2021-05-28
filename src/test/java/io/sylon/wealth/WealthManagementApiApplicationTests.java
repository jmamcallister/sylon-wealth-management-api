package io.sylon.wealth;

import io.sylon.wealth.model.dto.CreateWatchlistDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.sylon.wealth.util.TestData.json;
import static io.sylon.wealth.util.TestData.singleBackendSearchResponse;
import static io.sylon.wealth.util.TestData.tooManyRequestsResponse;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@ActiveProfiles("test")
@AutoConfigureWireMock(port=0)
@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WealthManagementApiApplicationTests {

  @Autowired
  WebTestClient webTestClient;

  @BeforeEach
  void beforeEach() {
    webTestClient = webTestClient.mutate().filter(basicAuthentication("test", "test")).build();
  }
  @Test
  void search_givenGoodRequest_shouldReturnGoodResponse() throws Exception {
    stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("SYMBOL_SEARCH"))
        .withQueryParam("keywords", equalTo("ACME"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(singleBackendSearchResponse("ACME", "Acme Corporation"))))
    );
    webTestClient.get()
        .uri("/search?query=ACME")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.total").isEqualTo(1)
        .jsonPath("$.results[0].name").isEqualTo("Acme Corporation");
  }

  @Test
  void search_givenTooManyRequests_shouldReturn429() throws Exception {
    stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("SYMBOL_SEARCH"))
        .withQueryParam("keywords", equalTo("ACME"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(tooManyRequestsResponse())))
    );
    webTestClient.get()
        .uri("/search?query=ACME")
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  void watchlist_create_givenDefaultWatchlistName_shouldReturnConflict() throws Exception {
    webTestClient.post()
        .uri("/watchlists")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(CreateWatchlistDto.builder().name("default").build()), CreateWatchlistDto.class)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CONFLICT)
        .expectBody()
        .jsonPath("$.error").isEqualTo(HttpStatus.CONFLICT.getReasonPhrase());
  }

  @Test
  void watchlist_create_givenExistingWatchlistName_shouldReturnConflict() throws Exception {

  }

  @Test
  void watchlist_create_givenNewWatchlistName_shouldReturnNewWatchlistId() throws Exception {
    webTestClient.post()
        .uri("/watchlists")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(CreateWatchlistDto.builder().name("New Watchlist").build()), CreateWatchlistDto.class)
        .exchange()
        .expectStatus().isEqualTo(HttpStatus.CREATED)
        .expectBody()
        .jsonPath("$.id").isNotEmpty();
  }

  @Test
  void watchlist_get_givenNoWatchlistsAddedByUser_shouldReturnDefaultEmptyWatchlist() throws Exception {
    webTestClient.get()
        .uri("/watchlists")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.total").isEqualTo(1)
        .jsonPath("$.watchlists").isArray()
        .jsonPath("$.watchlists.length()").isEqualTo(1)
        .jsonPath("$.watchlists[0].name").isEqualTo("default")
        .jsonPath("$.watchlists[0].id").isNotEmpty();
  }

  @Test
  void watchlist_get_givenGoodRequest_shouldReturnUsersWatchlists() throws Exception {

  }

  @Test
  void watchlist_get_givenNonExistentWatchlistId_shouldReturnNotFound() throws Exception {

  }

  @Test
  void watchlist_update_givenNewSymbolAndExistingWatchlist_shouldReturnSuccess() throws Exception {

  }

  @Test
  void watchlist_update_givenNonExistentWatchlist_shouldReturnNotFound() throws Exception {

  }

  @Test
  void watchlist_update_givenConflictingData_shouldReturnBadRequest() throws Exception {

  }

  @Test
  void watchlist_delete_givenExistingWatchlist_shouldReturnNoContent() throws Exception {

  }

  @Test
  void watchlist_deleteSymbol_givenExistingWatchlistAndExistingSymbol_shouldReturnNoContent() throws Exception {

  }

  @Test
  void watchlist_deleteSymbol_givenNonExistingWatchlist_shouldReturnNotFound() throws Exception {

  }
}
