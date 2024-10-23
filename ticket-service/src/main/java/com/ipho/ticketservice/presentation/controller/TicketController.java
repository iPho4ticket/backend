package com.ipho.ticketservice.presentation.controller;

import com.ipho.ticketservice.presentation.request.TicketRequestDto;
import com.ipho.ticketservice.application.service.TicketService;
import com.ticketing.authzfilter.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.ticketing.authzfilter.infrastructure.common.RoleType.Authority.USER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticket")
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasRole('"+ USER +"')")
    @PostMapping
    public ResponseEntity<?> reservationTicket(@RequestBody TicketRequestDto ticketRequestDto) {
        Long userId = SecurityUtil.getUserId();
        return ResponseEntity.ok(ticketService.reservationTicket(ticketRequestDto, userId));
    }

    @PreAuthorize("hasRole('"+ USER +"')")
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> searchTicketInfo(@PathVariable UUID ticketId) {
        Long userId = SecurityUtil.getUserId();
        return ResponseEntity.ok(ticketService.searchTicketInfo(ticketId, userId));
    }

    @PreAuthorize("hasRole('"+ USER +"')")
    @PutMapping("/{ticketId}/cancel")
    public ResponseEntity<?> cancelTicket(@PathVariable UUID ticketId) {
        Long userId = SecurityUtil.getUserId();
        return ResponseEntity.ok(ticketService.cancelTicket(ticketId, userId));
    }

}
