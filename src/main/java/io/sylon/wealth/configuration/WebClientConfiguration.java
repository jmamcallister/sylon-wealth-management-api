package io.sylon.wealth.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebClientConfiguration {

  @Value("${app.backend.url:https://www.alphavantage.co}")
  private String baseUrl;

  @Value("${app.backend.api.key:demo}")
  private String apiKey;

  @Value("${app.backend.timeout.read.seconds:10}")
  private int readTimeout;

  @Value("${app.backend.timeout.write.seconds:10}")
  private int writeTimeout;

  @Value("${app.backend.timeout.connect.millis:10000}")
  private int connectTimeout;

  @Bean
  public WebClient webClient() {
    HttpClient httpClient = HttpClient.create();
    httpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
    httpClient.doOnConnected(connection -> {
      connection.addHandlerLast(new ReadTimeoutHandler(readTimeout));
      connection.addHandlerLast(new WriteTimeoutHandler(writeTimeout));
    });

    Map<String, String> params = new HashMap<>(1);
    params.put("apikey", apiKey);

    return WebClient.builder()
        .baseUrl(baseUrl + "/query?apikey={apikey}")
        .defaultUriVariables(params)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filter(RequestLoggingFilter.logRequest())
        .build();
  }
}
