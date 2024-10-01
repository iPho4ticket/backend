package com.ipho4ticket.paymentservice.application.service;

import com.ipho4ticket.paymentservice.domain.model.Payment;
import com.ipho4ticket.paymentservice.domain.model.PaymentStatus;
import com.ipho4ticket.paymentservice.domain.repository.PaymentRepository;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    // TODO: Exception 공통 모듈 이후 전체적으로 수정

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        // 1. ticketId와 userId를 통해 티켓 유효성 검사(추후 외부 API를 통해 검증)

        // 2. 결제 생성
        Payment payment = createNewPayment(request);
        paymentRepository.save(payment);

        // TODO: 이 지점에서 트랜잭션 분리가 필요한가.. 고민
        /*
        1. 단일 트랜잭션에서 처리 -> 외부 결제에서 실패할 경우 기존에 생성된 결제가 없어짐
        대처방법 1-1. try-catch 를 통한 상태처리 (가장 유력)

        2. kafka를 이용한 비동기 처리
        -> 외부 queue를 타고 오다보니 결제 결과가 즉시 반환되지 않기 때문에 문제

        3. Async를 이용한 비동기 처리
        -> 내부 thread를 사용하여 간단한 비동기 처리 / 애플리케이션 부담이 늘어나면 결제 유실 가능성 있음
         */

        // 3. 외부 결제 API를 통한 결제 시행 / ex) 카카오페이 결제 모듈

        // 4. 외부 결제 성공 이후 결제 상태 변경
        payment.updateStatus(PaymentStatus.COMPLETED);

        // 5. 티켓 상태 변경(추후 외부 API를 통해 진행)

        return toResponseDTO(payment);
    }

    // 2. 결제 단건 조회
    public PaymentResponseDTO getPayment(UUID paymentId, Long currentUserId) throws AccessDeniedException {
        Payment payment = findPaymentAndCheckPermission(paymentId, currentUserId);
        return toResponseDTO(payment);
    }

    // 3. 결제 목록 조회
    public Page<PaymentResponseDTO> getAllPayments(Long currentUserId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(currentUserId, pageable);
        return payments.map(this::toResponseDTO);
    }

    // 4. 결제 내역 검색
    @Transactional
    public Page<PaymentResponseDTO> searchPayments(Map<String, String> searchParams, Pageable pageable) {
        Page<Payment> payments = paymentRepository.searchWithParams(searchParams, pageable);
        return payments.map(this::toResponseDTO);
    }

    // 5. 결제 취소
    @Transactional
    public PaymentResponseDTO cancelPayment(UUID paymentId, Long userId) throws AccessDeniedException {
        Payment payment = findPaymentAndCheckPermission(paymentId, userId);
        checkPaymentStatus(payment, PaymentStatus.COMPLETED);
        updatePaymentStatus(payment, PaymentStatus.CANCELED);

        return toResponseDTO(payment);
    }

    // Helper method to create new Payment entity
    private Payment createNewPayment(PaymentRequestDTO request) {
        return Payment.builder()
            .userId(request.userId())
            .ticketId(request.ticketId())
            .method(request.paymentMethod())
            .amount(BigDecimal.valueOf(request.amount()))
            .status(PaymentStatus.OPENED)
            .build();
    }

    // Helper method to find a Payment and check user permissions
    private Payment findPaymentAndCheckPermission(UUID paymentId, Long currentUserId) throws AccessDeniedException {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!payment.getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to access this payment");
        }

        return payment;
    }

    // Helper method to update the payment status
    private void updatePaymentStatus(Payment payment, PaymentStatus newStatus) {
        payment.updateStatus(newStatus);
        paymentRepository.save(payment); // 상태가 변경되면 save를 통해 변경 사항을 저장
    }

    // Helper method to check payment status
    private void checkPaymentStatus(Payment payment, PaymentStatus expectedStatus) {
        if (!payment.getStatus().equals(expectedStatus)) {
            throw new IllegalStateException("Payment status must be " + expectedStatus + " to perform this action");
        }
    }

    // Helper method to convert Payment entity to DTO
    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
            .paymentId(payment.getPaymentId())
            .userId(payment.getUserId())
            .ticketId(payment.getTicketId())
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .date(payment.getDate())
            .build();
    }
}
