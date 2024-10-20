package com.ipho4ticket.paymentservice.application.service;

import com.ipho.common.exception.payment.AccessDeniedPaymentException;
import com.ipho.common.exception.payment.InvalidPaymentStatusException;
import com.ipho.common.exception.payment.PaymentNotFoundException;
import com.ipho.common.exception.payment.PaymentProcessingException;
import com.ipho.common.exception.payment.TicketValidationException;
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
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final ClientTicketFeign clientTicketFeign;

    @Transactional
    public ReadyResponse createPayment(PaymentRequestDTO request) {
        String itemName = validateTicket(request.ticketId(), request.userId());

        Payment payment = getOrCreatePayment(request);

        try {
            return initiatePayment(request, payment, itemName);
        } catch (Exception e) {
            handlePaymentFailure(payment);
            throw new PaymentProcessingException("결제 준비 실패: " + e.getMessage());
        }
    }

    @Transactional(dontRollbackOn = {PaymentProcessingException.class,
        InvalidPaymentStatusException.class})
    public PaymentResponseDTO approvePayment(UUID paymentId, UUID ticketId, String pgToken) {
        Payment payment = findPaymentById(paymentId);
        validatePendingPayment(payment);

        try {
            processApproval(payment, pgToken);
            updateTicketStatus(ticketId);
            return toResponseDTO(payment);
        } catch (Exception e) {
            handlePaymentFailure(payment);
            throw new PaymentProcessingException("결제 승인 실패: " + e.getMessage());
        }
    }

    public PaymentResponseDTO getPayment(UUID paymentId, Long currentUserId) {
        Payment payment = findPaymentAndCheckPermission(paymentId, currentUserId);
        return toResponseDTO(payment);
    }

    public Page<PaymentResponseDTO> getAllPayments(Long currentUserId, Pageable pageable) {
        return paymentRepository.findByUserId(currentUserId, pageable)
            .map(this::toResponseDTO);
    }

    @Transactional
    public Page<PaymentResponseDTO> searchPayments(Map<String, String> searchParams,
        Pageable pageable) {
        return paymentRepository.searchWithParams(searchParams, pageable)
            .map(this::toResponseDTO);
    }

    @Transactional
    public ApproveResponse cancelPayment(UUID paymentId, Long userId, String tid,
        Integer cancelAmount, Integer cancelTaxFreeAmount, Integer cancelVatAmount) {
        Payment payment = findPaymentAndCheckPermission(paymentId, userId);
        validateCompletedPayment(payment);

        validateTicketCancel(payment.getTicketId(), payment.getUserId());

        ApproveResponse approveResponse = processCancel(payment, tid, cancelAmount,
            cancelTaxFreeAmount, cancelVatAmount);
        updatePaymentStatus(payment, PaymentStatus.CANCELED);
        updateTicketStatusCancel(payment.getTicketId());

        return approveResponse;
    }

    // 1. 티켓 유효성 검증
    private String validateTicket(UUID ticketId, Long userId) {
        ValidationResponse validationResponse = clientTicketFeign.validateTicket(ticketId, userId);
        if (!validationResponse.success()) {
            throw new TicketValidationException(validationResponse.message());
        }
        return validationResponse.message();
    }

    // 2. 결제 가져오기 또는 새로 생성
    private Payment getOrCreatePayment(PaymentRequestDTO request) {
        return paymentRepository.findByTicketId(request.ticketId())
            .map(payment -> {
                validatePaymentStatus(payment);
                return payment;
            })
            .orElseGet(() -> createNewPayment(request));
    }

    // 3. 결제 준비
    private ReadyResponse initiatePayment(PaymentRequestDTO request, Payment payment,
        String itemName) {
        PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(
            request.paymentMethod());
        ReadyResponse readyResponse = processor.payReady(itemName, payment.getAmount(),
            request.ticketId(), payment.getPaymentId());

        payment.setTid(readyResponse.getTid());
        updatePaymentStatus(payment, PaymentStatus.PENDING);
        return readyResponse;
    }

    // 4. 결제 승인 처리
    private ApproveResponse processApproval(Payment payment, String pgToken) {
        PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(
            payment.getMethod());
        ApproveResponse approveResponse = processor.payApprove(payment.getTid(), pgToken);
        updatePaymentStatus(payment, PaymentStatus.COMPLETED);
        return approveResponse;
    }

    // 5. 결제 실패 처리
    private void handlePaymentFailure(Payment payment) {
        updatePaymentStatus(payment, PaymentStatus.FAILED);
    }

    // 6. 티켓 상태 업데이트
    private void updateTicketStatus(UUID ticketId) {
        ValidationResponse validationResponse = clientTicketFeign.changeTicketStatus(ticketId);
        if (!validationResponse.success()) {
            throw new TicketValidationException(validationResponse.message());
        }
    }

    private void updateTicketStatusCancel(UUID ticketId) {
        ValidationResponse validationResponse = clientTicketFeign.changeTicketStatusCancel(
            ticketId);
        if (!validationResponse.success()) {
            throw new TicketValidationException(validationResponse.message());
        }
    }

    // 7. 결제 취소 처리
    private ApproveResponse processCancel(Payment payment, String tid, Integer cancelAmount,
        Integer cancelTaxFreeAmount, Integer cancelVatAmount) {
        PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(
            payment.getMethod());
        return processor.cancelPayment(tid, cancelAmount, cancelTaxFreeAmount, cancelVatAmount);
    }

    // Helper methods

    private Payment createNewPayment(PaymentRequestDTO request) {
        return Payment.builder()
            .userId(request.userId())
            .ticketId(request.ticketId())
            .method(request.paymentMethod())
            .amount(BigDecimal.valueOf(request.amount()))
            .status(PaymentStatus.OPENED)
            .build();
    }

    private Payment findPaymentAndCheckPermission(UUID paymentId, Long currentUserId) {
        Payment payment = findPaymentById(paymentId);
        if (!payment.getUserId().equals(currentUserId)) {
            throw new AccessDeniedPaymentException("이 결제에 접근할 권한이 없습니다.");
        }
        return payment;
    }

    private Payment findPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("결제를 찾을 수 없습니다."));
    }

    private void updatePaymentStatus(Payment payment, PaymentStatus newStatus) {
        payment.updateStatus(newStatus);
        paymentRepository.save(payment);
    }

    private void validatePaymentStatus(Payment payment) {
        if (!PaymentStatus.PENDING.equals(payment.getStatus()) && !PaymentStatus.FAILED.equals(
            payment.getStatus())) {
            throw new InvalidPaymentStatusException("이미 완료된 결제가 있습니다.");
        }
    }

    private void validatePendingPayment(Payment payment) {
        if (!PaymentStatus.PENDING.equals(payment.getStatus())) {
            throw new InvalidPaymentStatusException("결제가 대기 상태여야 합니다.");
        }
    }

    private void validateCompletedPayment(Payment payment) {
        if (!PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            throw new InvalidPaymentStatusException("결제가 완료된 상태여야 합니다.");
        }
    }

    private void validateTicketCancel(UUID ticketId, Long userId) {
        ValidationResponse validationResponse = clientTicketFeign.checkTicketCancel(ticketId,
            userId);
        if (!validationResponse.success()) {
            throw new TicketValidationException(validationResponse.message());
        }
    }

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
