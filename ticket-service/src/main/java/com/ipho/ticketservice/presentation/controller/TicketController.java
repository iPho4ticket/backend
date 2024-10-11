package com.ipho.ticketservice.presentation.controller;

import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.application.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketController {

    private final TicketService ticketService;
    // 인증/인가는 방법은 추후 논의

    @PostMapping
    public ResponseEntity<?> reservationTicket(@RequestBody TicketRequestDto ticketRequestDto) {
        return ResponseEntity.ok(ticketService.reservationTicket(ticketRequestDto));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> searchTicketInfo(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ticketService.searchTicketInfo(ticketId));
    }

    @PutMapping("/{ticketId}/cancel")
    public ResponseEntity<?> cancelTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ticketService.cancelTicket(ticketId));
    }

    @PostMapping("/seat-service/pending/{ticketId}")
    public ResponseEntity<?> pending(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ticketService.pending(ticketId));
    }

}
