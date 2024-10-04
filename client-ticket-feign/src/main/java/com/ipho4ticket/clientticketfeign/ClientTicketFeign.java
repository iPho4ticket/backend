package com.ipho4ticket.clientticketfeign;

import com.ipho4ticket.clientticketfeign.dto.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ticket-api", url = "${ticket.api.url}")
public interface ClientTicketFeign {

    // 티켓 상태 및 유저 검증
    @GetMapping("/api/v1/internal/ticket/{ticket_id}")
    ValidationResponse validateTicket(
        @PathVariable("ticket_id") Long ticketId,
        @RequestParam("user_id") Long userId
    );

    // 결제 완료 이후 티켓 상태 변경
    @PostMapping("/api/v1/internal/ticket/{ticket_id}")
    ValidationResponse changeTicketStatus(@PathVariable("ticket_id") Long ticketId);
}
