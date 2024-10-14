package com.ipho4ticket.clientticketfeign;

import com.ipho4ticket.clientticketfeign.dto.ValidationResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "ticket-service", url = "http://ticket-service:19095")
public interface ClientTicketFeign {

    // 티켓 상태 및 유저 검증
    @GetMapping("/api/v1/internal/ticket/{ticket_id}")
    ValidationResponse validateTicket(
        @PathVariable("ticket_id") UUID ticketId,
        @RequestParam("user_id") Long userId
    );

    // 결제 완료 이후 티켓 상태 변경
    @PostMapping("/api/v1/internal/ticket/{ticket_id}")
    ValidationResponse changeTicketStatus(@PathVariable("ticket_id") UUID ticketId);

    // 결제 취소 요청 시 티켓 상태 확인 및 취소가능 여부 확인
    @GetMapping("/api/v1/internal/ticket/{ticket_id}/cancel")
    ValidationResponse checkTicketCancel(
        @PathVariable("ticket_id") UUID ticketId,
        @RequestParam("user_id") Long userId
    );

    // 결제 취소 로직 이후 실제 티켓 및 좌석 상태 변경
    @PostMapping("/api/v1/internal/ticket/{ticket_id}/cancel")
    ValidationResponse changeTicketStatusCancel(@PathVariable("ticket_id") UUID ticketId);
}
