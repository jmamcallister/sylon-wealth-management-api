package io.sylon.wealth;

import io.sylon.wealth.model.dto.CreateWatchlistDto;
import io.sylon.wealth.model.dto.UpdateWatchlistDto;
import io.sylon.wealth.model.dto.WatchlistDto;
import io.sylon.wealth.model.dto.WatchlistsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.sylon.wealth.util.TestData.json;
import static io.sylon.wealth.util.TestData.singleBackendQuoteResponse;
import static io.sylon.wealth.util.TestData.singleBackendSearchResponse;
import static io.sylon.wealth.util.TestData.tooManyRequestsResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    deleteAllWatchlists();

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
  void watchlist_getById_givenNonExistentWatchlistId_shouldReturnNotFound() throws Exception {
    webTestClient.get()
        .uri("/watchlists/{id}", "a-fake-id")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.errorMessage").isEqualTo("Watchlist with id a-fake-id not found");
  }

  @Test
  void watchlist_getById_givenNewDefaultWatchlistId_shouldReturnWatchlistWithNoSymbols() throws Exception {
    deleteAllWatchlists();

    EntityExchangeResult<WatchlistsResponse> result = webTestClient.get()
        .uri("/watchlists")
        .exchange()
        .expectStatus().isOk()
        .expectBody(WatchlistsResponse.class)
        .returnResult();

    WatchlistsResponse watchlistsResponse = result.getResponseBody();
    assertNotNull(watchlistsResponse);
    List<WatchlistDto> watchlists = result.getResponseBody().getWatchlists();
    assertNotNull(watchlists);
    assertFalse(watchlists.isEmpty());
    assertEquals(1, watchlists.size());
    String id = watchlists.get(0).getId();
    assertNotNull(id);

    webTestClient.get()
        .uri("/watchlists/{id}", id)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(id)
        .jsonPath("$.name").isEqualTo("default")
        .jsonPath("$.symbols").isArray()
        .jsonPath("$.symbols").isEmpty();
  }

  @Test
  void watchlist_refresh_givenWatchlistWithSymbols_shouldReturnDetails() throws Exception {
    stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
        .withQueryParam("symbol", equalTo("ACME"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(singleBackendQuoteResponse("ACME", "100.00"))))
    );
    stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("GLOBAL_QUOTE"))
        .withQueryParam("symbol", equalTo("GME"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(singleBackendQuoteResponse("GME", "200.00"))))
    );

    deleteAllWatchlists();
    String id = getDefaultWatchlistId();

    webTestClient.put()
        .uri("/watchlists/{id}", id)
        .body(Mono.just(UpdateWatchlistDto.builder().symbols(List.of("GME", "ACME")).build()), UpdateWatchlistDto.class)
        .exchange()
        .expectStatus().isOk();

    webTestClient.get()
        .uri("/watchlists/{id}/quotes", id)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.quotes").isArray()
        .jsonPath("$.quotes.length()").isEqualTo(2)
        .jsonPath("$.quotes[0].symbol").isNotEmpty()
        .jsonPath("$.quotes[0].price").isNotEmpty();
  }

  @Test
  void watchlist_update_givenNewSymbolAndExistingWatchlist_shouldReturnSuccess() throws Exception {
    deleteAllWatchlists();
    WatchlistsResponse watchlistsResponse = getAllWatchlists();

    assertNotNull(watchlistsResponse);
    List<WatchlistDto> watchlists = watchlistsResponse.getWatchlists();
    assertNotNull(watchlists);
    assertFalse(watchlists.isEmpty());
    assertEquals(1, watchlists.size());
    String id = watchlists.get(0).getId();
    assertNotNull(id);

    webTestClient.put()
        .uri("/watchlists/{id}", id)
        .body(Mono.just(UpdateWatchlistDto.builder().symbols(List.of("GME")).build()), UpdateWatchlistDto.class)
        .exchange()
        .expectStatus().isOk();

    getAllWatchlists();

    // Idempotent
    webTestClient.put()
        .uri("/watchlists/{id}", id)
        .body(Mono.just(UpdateWatchlistDto.builder().symbols(List.of("GME")).build()), UpdateWatchlistDto.class)
        .exchange()
        .expectStatus().isOk();

    getAllWatchlists();
  }

  @Test
  void watchlist_update_givenNonExistentWatchlist_shouldReturnNotFound() throws Exception {
    deleteAllWatchlists();

    webTestClient.put()
        .uri("/watchlists/{id}", "fake")
        .body(Mono.just(UpdateWatchlistDto.builder().symbols(List.of("GME")).build()), UpdateWatchlistDto.class)
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void watchlist_update_givenNoData_shouldReturnBadRequest() throws Exception {
    deleteAllWatchlists();
    String id = getDefaultWatchlistId();
    webTestClient.put()
        .uri("/watchlists/{id}", id)
        .body(Mono.just(UpdateWatchlistDto.builder().build()), UpdateWatchlistDto.class)
        .exchange()
        .expectStatus().isBadRequest();
  }

  @Test
  void watchlist_delete_givenExistingWatchlist_shouldReturnNoContent() throws Exception {
    deleteAllWatchlists();
    WatchlistsResponse watchlistsResponse = getAllWatchlists();

    assertNotNull(watchlistsResponse);
    List<WatchlistDto> watchlists = watchlistsResponse.getWatchlists();
    assertNotNull(watchlists);
    assertFalse(watchlists.isEmpty());
    assertEquals(1, watchlists.size());
    String id = watchlists.get(0).getId();
    assertNotNull(id);

    webTestClient.delete()
        .uri("/watchlists/{id}", id)
        .exchange()
        .expectStatus().isNoContent();
  }

  @Test
  void watchlist_delete_givenNonExistingWatchlist_shouldReturnNotFound() throws Exception {
    webTestClient.delete()
        .uri("/watchlists/{id}", "not-a-real-id")
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.errorMessage").isEqualTo("Watchlist with id not-a-real-id not found");
  }

  @Test
  void watchlist_deleteSymbol_givenExistingWatchlistAndExistingSymbol_shouldReturnNoContent() throws Exception {
    deleteAllWatchlists();
    String id = getDefaultWatchlistId();

    webTestClient.put()
        .uri("/watchlists/{id}", id)
        .body(Mono.just(UpdateWatchlistDto.builder().symbols(List.of("GME")).build()), UpdateWatchlistDto.class)
        .exchange()
        .expectStatus().isOk();

    webTestClient.delete()
        .uri("/watchlists/{id}/GME", id)
        .exchange()
        .expectStatus().isNoContent();
  }

  @Test
  void watchlist_deleteSymbol_givenNonExistingWatchlist_shouldReturnNotFound() throws Exception {
    webTestClient.delete()
        .uri("/watchlists/{id}/{symbol}", "fake", "also_fake")
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void watchlist_deleteSymbol_givenExistingWatchlistAndNonExistentSymbol_shouldReturnNotFound() throws Exception {
    deleteAllWatchlists();
    String id = getDefaultWatchlistId();

    webTestClient.delete()
        .uri("/watchlists/{id}/GME", id)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .jsonPath("$.errorMessage").isEqualTo(String.format("No symbol %s found in watchlist id %s", "GME", id));
  }

  private WatchlistsResponse getAllWatchlists() {
    EntityExchangeResult<WatchlistsResponse> result = webTestClient.get()
        .uri("/watchlists")
        .exchange()
        .expectStatus().isOk()
        .expectBody(WatchlistsResponse.class)
        .returnResult();
    return result.getResponseBody();
  }

  private String getDefaultWatchlistId() {
    WatchlistsResponse watchlistsResponse = getAllWatchlists();
    assertNotNull(watchlistsResponse);
    List<WatchlistDto> watchlists = watchlistsResponse.getWatchlists();
    assertNotNull(watchlists);
    assertFalse(watchlists.isEmpty());
    assertEquals(1, watchlists.size());
    String id = watchlists.get(0).getId();
    assertNotNull(id);
    return id;
  }

  private void deleteAllWatchlists() {
    EntityExchangeResult<WatchlistsResponse> result = webTestClient.get()
        .uri("/watchlists")
        .exchange()
        .expectStatus().isOk()
        .expectBody(WatchlistsResponse.class)
        .returnResult();

    WatchlistsResponse watchlistsResponse = result.getResponseBody();
    assertNotNull(watchlistsResponse);
    List<WatchlistDto> watchlists = result.getResponseBody().getWatchlists();

    watchlists.parallelStream().forEach(w -> webTestClient.delete()
        .uri("/watchlists/{id}", w.getId())
        .exchange()
        .expectStatus().isNoContent()
    );
  }
}
