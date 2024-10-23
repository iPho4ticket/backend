package com.ipho4ticket.paymentservice.presentation.controller;

import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.application.dto.PaymentInfoResponse;
import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import com.ipho4ticket.paymentservice.application.service.PaymentService;
import com.ipho4ticket.paymentservice.infrastructure.external.KakaoPayService;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import com.ticketing.authzfilter.security.SecurityUtil;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.ticketing.authzfilter.infrastructure.common.RoleType.Authority.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final KakaoPayService kakaoPayService;

    // 1. 결제 등록 (POST)
    @PostMapping
    @PreAuthorize("hasRole('"+ USER +"')")
    public ResponseEntity<Map<String, String>> createPayment(
        @RequestBody PaymentRequestDTO request) {
        // 결제 생성 및 카카오페이 결제 준비
        Long userId = SecurityUtil.getUserId();
        ReadyResponse readyResponse = paymentService.createPayment(request, userId);

        // 결제 준비 완료 후, 카카오페이 결제 페이지로 리다이렉트 -> 프론트 구현 시 해당 방법으로 변경
        // 결제 준비 완료 후, 리다이렉트 URL을 JSON 응답으로 반환
        return ResponseEntity.ok()
            .body(Map.of("redirect_url", readyResponse.getNext_redirect_pc_url()));

    }

    // 결제 승인 요청을 처리하는 메소드
    // 카카오페이 리다이랙팅 url로 보안설정 X
    @GetMapping("/approve")
    public ResponseEntity<PaymentResponseDTO> approvePayment(
        @RequestParam("payment_id") UUID paymentId,
        @RequestParam("ticket_id") UUID ticketId,
        @RequestParam("pg_token") String pgToken
    ) {

        // 결제 승인 로직 호출
        PaymentResponseDTO paymentResponse = paymentService.approvePayment(paymentId, ticketId,
            pgToken);

        // 승인 완료된 결제 정보를 반환
        return ResponseEntity.ok(paymentResponse);
    }

    // 결제 취소 요청을 처리하는 메소드
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('"+ USER +"')")
    public ResponseEntity<ApproveResponse> cancelPayment(
        @RequestParam("payment_id") UUID paymentId,
        @RequestParam("tid") String tid) {
        Long userId = SecurityUtil.getUserId();

        // 1. 결제 정보 조회
        PaymentInfoResponse paymentInfo = kakaoPayService.getPaymentInfo(tid);

        System.out.println(paymentInfo.toString());

        // 2. 남은 취소 가능 금액을 가져와서 취소 금액 결정
        Integer cancelAmount = paymentInfo.getAmount().getTotal();
        Integer cancelTaxFreeAmount = paymentInfo.getAmount().getTaxFree();
        Integer cancelVatAmount = paymentInfo.getAmount().getVat();

        // 3. 결제 취소 요청
        ApproveResponse cancelResponse = paymentService.cancelPayment(paymentId, userId, tid,
            cancelAmount, cancelTaxFreeAmount, cancelVatAmount);

        // 취소 완료된 결제 정보를 반환
        return ResponseEntity.ok(cancelResponse);
    }

    // 2. 결제 내역 단건 조회 (GET)
    @GetMapping("/{payment_id}")
    @PreAuthorize("hasRole('"+ USER +"')")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable UUID payment_id)
    {
        Long userId = SecurityUtil.getUserId();
        PaymentResponseDTO payment = paymentService.getPayment(payment_id, userId);
        return ResponseEntity.ok(payment);
    }

    // 3. 결제 목록 조회 (GET)
    @GetMapping
    @PreAuthorize("hasRole('"+ USER +"')")
    public ResponseEntity<Page<PaymentResponseDTO>> getAllPayments(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getUserId();

        // Pageable 객체 생성 (페이지 번호와 페이지 크기)
        Pageable pageable = PageRequest.of(page, size);

        // 서비스에서 결제 목록 조회
        Page<PaymentResponseDTO> paymentPage = paymentService.getAllPayments(userId,
            pageable);

        return ResponseEntity.ok(paymentPage);
    }


    // 4. 결제 내역 검색 (GET)
    @GetMapping("/search")
    @PreAuthorize("hasRole('"+ MASTER +"')")
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

}
