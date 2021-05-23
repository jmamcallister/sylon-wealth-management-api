package io.sylon.wealth.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.sylon.wealth.exception.BackendErrorMessageException;
import io.sylon.wealth.exception.BackendInformationException;
import io.sylon.wealth.exception.BackendRateLimitExceededException;
import io.sylon.wealth.model.dto.BackendErrorMessageResponse;
import io.sylon.wealth.model.dto.BackendInformationResponse;
import io.sylon.wealth.model.dto.SearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.sylon.wealth.util.TestData.json;
import static io.sylon.wealth.util.TestData.singleBackendSearchResponse;
import static io.sylon.wealth.util.TestData.tooManyRequestsResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlphaVantageSearchServiceTest {

  private WireMockServer mockServer;

  private AlphaVantageSearchService searchService;

  @BeforeEach
  void beforeEach() {
    mockServer = new WireMockServer(options().dynamicPort());
    mockServer.start();
    configureFor(mockServer.port());

    WebClient webClient = WebClient.builder()
        .baseUrl(mockServer.baseUrl() + "/query")
        .build();

    searchService = new AlphaVantageSearchService(webClient);
  }

  @Test
  void search_givenGoodRequest_shouldReturnGoodResponse() throws Exception {
    mockServer.stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("SYMBOL_SEARCH"))
        .withQueryParam("keywords", equalTo("ACME"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(singleBackendSearchResponse("ACME", "Acme Corporation"))))
    );

    SearchResponse response = searchService.search("ACME");

    assertEquals(1, response.getTotal());
    assertEquals("ACME", response.getResults().get(0).getSymbol());
    assertEquals("Acme Corporation", response.getResults().get(0).getName());
  }

  @Test
  void search_givenTooManyRequests_shouldThrowException() throws Exception {
    mockServer.stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("SYMBOL_SEARCH"))
        .withQueryParam("keywords", equalTo("AAPL"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(tooManyRequestsResponse())))
    );
    assertThrows(BackendRateLimitExceededException.class, () -> searchService.search("AAPL"));
  }

  @Test
  void search_givenWrongApiKey_shouldThrowException() throws Exception {
    mockServer.stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("SYMBOL_SEARCH"))
        .withQueryParam("keywords", equalTo("GME"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(wrongApiKeyResponse())))
    );
    assertThrows(BackendErrorMessageException.class, () -> searchService.search("GME"));
  }

  @Test
  void search_givenDemoKeyUsedIncorrectly_shouldThrowException() throws Exception {
    mockServer.stubFor(get(urlPathEqualTo("/query"))
        .withQueryParam("function", equalTo("SYMBOL_SEARCH"))
        .withQueryParam("keywords", equalTo("WORK"))
        .willReturn(aResponse().withStatus(HttpStatus.OK.value()).withBody(json(demoKeyResponse())))
    );
    assertThrows(BackendInformationException.class, () -> searchService.search("WORK"));

  }

  private BackendErrorMessageResponse wrongApiKeyResponse() {
    return BackendErrorMessageResponse.builder()
        .errorMessage("Wrong API Key")
        .build();
  }

  private BackendInformationResponse demoKeyResponse() {
    return BackendInformationResponse.builder()
        .information("You are using the demo key incorrectly")
        .build();
  }
}