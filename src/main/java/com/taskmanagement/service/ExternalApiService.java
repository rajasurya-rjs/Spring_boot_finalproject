package com.taskmanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ExternalApiService {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiService.class);

    private final WebClient webClient;

    @Value("${application.external-api.currency-exchange-url}")
    private String currencyExchangeUrl;

    public ExternalApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Map<String, Object> convertCurrency(String from, String to, Double amount) {
        try {
            String url = String.format("%s/convert?from=%s&to=%s&amount=%.2f",
                    currencyExchangeUrl, from, to, amount);

            log.info("Calling currency exchange API: {}", url);

            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Currency conversion successful: {} {} -> {}", amount, from, to);
            return response;

        } catch (Exception e) {
            log.error("Failed to convert currency", e);
            throw new RuntimeException("Currency conversion failed: " + e.getMessage());
        }
    }
}
