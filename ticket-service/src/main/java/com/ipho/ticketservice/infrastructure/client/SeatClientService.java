package com.ipho.ticketservice.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j(topic = "seat-client")
public class SeatClientService {
    private final WebClient webClient;

    @Autowired
    public SeatClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://seat-service:19094").build();
    }

    public Mono<Void> requestRegisterTopic(String topic, UUID eventId) {
        return webClient.post()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/v1/internal/seats/subscribe")
                    .queryParam("topic", topic)
                    .queryParam("eventId", eventId)
                    .build())
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                return Mono.error(new RuntimeException("Client error"));
            })
            .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                return Mono.error(new RuntimeException("Server error"));
            })
            .bodyToMono(Void.class);  // No response body expected
    }
}
