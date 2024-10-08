package com.ipho4ticket.paymentservice.presentation.controller;

import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import com.ipho4ticket.paymentservice.application.service.PaymentService;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    Long exampleUserId = 1L;

    // 1. 결제 등록 (POST)
    @PostMapping
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody PaymentRequestDTO request) {
        // 결제 생성 및 카카오페이 결제 준비
        ReadyResponse readyResponse = paymentService.createPayment(request);

        // 결제 준비 완료 후, 카카오페이 결제 페이지로 리다이렉트 -> 프론트 구현 시 해당 방법으로 변경
        // 결제 준비 완료 후, 리다이렉트 URL을 JSON 응답으로 반환
        return ResponseEntity.ok()
            .body(Map.of("redirect_url", readyResponse.getNext_redirect_pc_url()));

    }

    // 결제 승인 요청을 처리하는 메소드
    @GetMapping("/approve")
    public ResponseEntity<PaymentResponseDTO> approvePayment(
        @RequestParam("payment_id") UUID paymentId,
        @RequestParam("pg_token") String pgToken
        ) {

        // 결제 승인 로직 호출
        PaymentResponseDTO paymentResponse = paymentService.approvePayment(paymentId, pgToken);

        // 승인 완료된 결제 정보를 반환
        return ResponseEntity.ok(paymentResponse);
    }


    // 2. 결제 내역 단건 조회 (GET)
    @GetMapping("/{payment_id}")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable UUID payment_id)
        throws AccessDeniedException {
        PaymentResponseDTO payment = paymentService.getPayment(payment_id, exampleUserId);
        return ResponseEntity.ok(payment);
    }

    // 3. 결제 목록 조회 (GET)
    @GetMapping
    public ResponseEntity<Page<PaymentResponseDTO>> getAllPayments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {

        // 현재 사용자 ID 가져오기 (Spring Security)
        // Long currentUserId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        // Pageable 객체 생성 (페이지 번호와 페이지 크기)
        Pageable pageable = PageRequest.of(page, size);

        // 서비스에서 결제 목록 조회
        Page<PaymentResponseDTO> paymentPage = paymentService.getAllPayments(exampleUserId,
            pageable);

        return ResponseEntity.ok(paymentPage);
    }


    // 4. 결제 내역 검색 (GET)
    @GetMapping("/search")
    public ResponseEntity<Page<PaymentResponseDTO>> searchPayments(
        @RequestParam Map<String, String> searchParams,
        @RequestParam(defaultValue = "0") int page,   // 페이지 번호 (기본값 0)
        @RequestParam(defaultValue = "10") int size   // 페이지 크기 (기본값 10)
    ) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        // 페이징된 결제 내역 검색
        Page<PaymentResponseDTO> payments = paymentService.searchPayments(searchParams, pageable);

        return ResponseEntity.ok(payments);
    }


    // 5. 결제 취소 (DELETE)
    @DeleteMapping("/{payment_id}")
    public ResponseEntity<PaymentResponseDTO> cancelPayment(@PathVariable UUID payment_id)
        throws AccessDeniedException {

        PaymentResponseDTO payment = paymentService.cancelPayment(payment_id, exampleUserId);
        return ResponseEntity.ok(payment);
    }
}
