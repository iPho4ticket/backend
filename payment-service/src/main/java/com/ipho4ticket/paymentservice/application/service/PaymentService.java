package com.ipho4ticket.paymentservice.application.service;

import com.ipho4ticket.clientticketfeign.ClientTicketFeign;
import com.ipho4ticket.clientticketfeign.dto.ValidationResponse;
import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import com.ipho4ticket.paymentservice.application.factory.PaymentProcessorFactory;
import com.ipho4ticket.paymentservice.domain.model.Payment;
import com.ipho4ticket.paymentservice.domain.model.PaymentStatus;
import com.ipho4ticket.paymentservice.domain.repository.PaymentRepository;
import com.ipho4ticket.paymentservice.domain.service.PaymentProcessor;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Optional;
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
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final ClientTicketFeign clientTicketFeign;

    @Transactional
    public ReadyResponse createPayment(PaymentRequestDTO request) {
        PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(request.paymentMethod());

        // 1. ticketId와 userId를 통해 티켓 유효성 검사(추후 외부 API를 통해 검증)
        ValidationResponse validationResponse = clientTicketFeign.validateTicket(request.ticketId(), request.userId());
        if (!validationResponse.success()){
            throw new IllegalStateException(validationResponse.message());
        }

        // 2. 결제 생성
        Optional<Payment> existingPaymentOpt = paymentRepository.findByTicketId(request.ticketId());

        Payment payment;

        if (existingPaymentOpt.isPresent()) {
            payment = existingPaymentOpt.get();

            // 결제 상태가 완료 혹은 취소되지 않은 경우에만 덮어쓰기 (ex. PENDING이나 FAILED 상태)
            if (!PaymentStatus.PENDING.equals(payment.getStatus()) && !PaymentStatus.FAILED.equals(payment.getStatus())) {
                throw new IllegalStateException("이미 완료된 결제가 있습니다.");
            }

        } else {
            // 새로운 결제를 생성
            payment = createNewPayment(request);
        }

        paymentRepository.save(payment);

        // 3. 외부 결제 API를 통한 결제 시행 / ex) 카카오페이 결제 모듈

        try {
            ReadyResponse readyResponse = processor.payReady("validationResponse.message()", payment.getAmount(), request.ticketId(), payment.getPaymentId());

            // 외부 결제가 성공적으로 준비된 경우, TID 저장 및 상태 업데이트
            payment.setTid(readyResponse.getTid());
            payment.updateStatus(PaymentStatus.PENDING); // 결제 준비 상태로 변경
            paymentRepository.save(payment);

            return readyResponse;

        } catch (Exception e) {
            // 결제 준비 실패 시, 결제 상태를 실패로 업데이트
            payment.updateStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalArgumentException("결제 준비 실패: " + e.getMessage());
        }

    }

    // TODO: 에러 처리 상세화 이후 dontRollbackOn 구분
    @Transactional(dontRollbackOn = {IllegalArgumentException.class, IllegalStateException.class})
    public PaymentResponseDTO approvePayment(UUID paymentId, UUID ticketId ,String pgToken) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new IllegalStateException("결제가 대기 상태여야 합니다.");
        }

        try {
            // 3. 외부 결제 승인 API 호출
            PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(payment.getMethod());
            ApproveResponse approveResponse = processor.payApprove(payment.getTid(), pgToken);

            // 승인 성공 시 결제 상태를 완료로 변경
            payment.updateStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            // 티켓 상태 업데이트 (추후 외부 API 통해 처리 가능)
            ValidationResponse validationResponse = clientTicketFeign.changeTicketStatus(ticketId);

            // 티켓 상태 실패 시
            /*
            TODO: 결제 성공 후 티켓 상태 변경 실패 시 리트라이 3번 이후 카카오페이 결제 취소 호출
             */
            if (!validationResponse.success()){
                throw new IllegalStateException(validationResponse.message());
            }
            return toResponseDTO(payment);

        } catch (Exception e) {
            // 결제 승인 실패 시 상태를 실패로 업데이트
            payment.updateStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalArgumentException("결제 승인 실패: " + e.getMessage());
        }
    }




    // 2. 결제 단건 조회
    public PaymentResponseDTO getPayment(UUID paymentId, Long currentUserId)
        throws AccessDeniedException {
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
    public Page<PaymentResponseDTO> searchPayments(Map<String, String> searchParams,
        Pageable pageable) {
        Page<Payment> payments = paymentRepository.searchWithParams(searchParams, pageable);
        return payments.map(this::toResponseDTO);
    }

    // 5. 결제 취소
    @Transactional
    public ApproveResponse cancelPayment(UUID paymentId, Long userId,  String tid,
        Integer cancelAmount, Integer cancelTaxFreeAmount, Integer cancelVatAmount)
        throws AccessDeniedException {
        Payment payment = findPaymentAndCheckPermission(paymentId, userId);
        checkPaymentStatus(payment, PaymentStatus.COMPLETED);

        // 티켓과 feign 요청으로 검증하는 로직 추가
        ValidationResponse validationResponse = clientTicketFeign.checkTicketCancel(payment.getTicketId(), payment.getUserId());
        if (!validationResponse.success()){
            throw new IllegalStateException(validationResponse.message());
        }

        PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(payment.getMethod());
        ApproveResponse approveResponse = processor.cancelPayment(tid, cancelAmount, cancelTaxFreeAmount, cancelVatAmount);

        updatePaymentStatus(payment, PaymentStatus.CANCELED);

        ValidationResponse validationPostResponse = clientTicketFeign.changeTicketStatusCancel(payment.getTicketId());
        if (!validationPostResponse.success()){
            throw new IllegalStateException(validationPostResponse.message());
        }

        return approveResponse;
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
    private Payment findPaymentAndCheckPermission(UUID paymentId, Long currentUserId)
        throws AccessDeniedException {
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
            throw new IllegalStateException(
                "Payment status must be " + expectedStatus + " to perform this action");
        }
    }

    // Helper method to convert Payment entity to DTO
    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
            .paymentId(payment.getPaymentId())
            .userId(payment.getUserId())
            .ticketId(payment.getTicketId())
            .tid(payment.getTid())
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .date(payment.getDate())
            .build();
    }
}
