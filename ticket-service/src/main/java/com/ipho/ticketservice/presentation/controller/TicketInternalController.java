package com.ipho.ticketservice.presentation.controller;

import com.ipho.ticketservice.application.service.TicketService;
import com.ipho.ticketservice.infrastructure.client.ValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/ticket")
public class TicketInternalController {

    private final TicketService ticketService;

    @GetMapping("/{ticket_id}")
    public ResponseEntity<ValidationResponse> validateTicket(
            @PathVariable("ticket_id") UUID ticketId,
            @RequestParam("user_id") Long userId) {

        return ResponseEntity.ok(ticketService.validateTicket(ticketId, userId));

    }

    @PostMapping("/api/v1/internal/ticket/{ticket_id}")
    public ResponseEntity<ValidationResponse> changeTicketStatus(@PathVariable("ticket_id") UUID ticketId) {
        return ResponseEntity.ok(ticketService.completePayment(ticketId));
    }

}
