package com.ipho4ticket.seatservice.infra;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j(topic = "ticket-client")
public class TicketClientService {
    private final WebClient webClient;

    @Autowired
    public TicketClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://ticket-service:19095").build();
    }

    public Mono<Void> requestRegisterTopic(String topic, UUID eventId) {
        System.out.println("나 메시지 보내기 전에 들어옴");
        return webClient.post()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/api/v1/internal/ticket/subscribe")
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
