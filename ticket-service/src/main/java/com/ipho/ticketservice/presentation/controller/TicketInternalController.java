package com.ipho.ticketservice.presentation.controller;

import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.presentation.response.ValidationResponse;
import com.ipho.ticketservice.infrastructure.messaging.DynamicKafkaListener;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/ticket")
public class TicketInternalController {

    private final TicketService ticketService;
    private final DynamicKafkaListener dynamicKafkaListener;


    @GetMapping("/{ticket_id}")
    public ResponseEntity<ValidationResponse> validateTicket(
            @PathVariable("ticket_id") UUID ticketId,
            @RequestParam("user_id") Long userId) {

        return ResponseEntity.ok(ticketService.validateTicket(ticketId, userId));
    }

    @PostMapping("/{ticket_id}")
    public ResponseEntity<ValidationResponse> changeTicketStatus(@PathVariable("ticket_id") UUID ticketId) {
        return ResponseEntity.ok(ticketService.completePayment(ticketId));
    }

    @GetMapping("/{ticket_id}/cancel")
    public ResponseEntity<ValidationResponse> checkTicketCancel(@PathVariable("ticket_id") UUID ticketId,
                                                         @RequestParam("user_id") Long userId) {
        return ResponseEntity.ok(ticketService.checkTicketCancel(ticketId, userId));
    }


    @PostMapping("/{ticket_id}/cancel")
    public ResponseEntity<ValidationResponse> changeTicketStatusCancel(@PathVariable("ticket_id") UUID ticketId) {
        return ResponseEntity.ok(ticketService.cancelPayment(ticketId));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String topic, @RequestParam UUID eventId) {
        dynamicKafkaListener.startListener(topic, eventId);
        return ResponseEntity.ok("Subscribed to topic: " + topic);
    }

}
