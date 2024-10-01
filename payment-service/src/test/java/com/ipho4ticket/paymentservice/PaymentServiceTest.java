package com.ipho4ticket.paymentservice;

import com.ipho4ticket.paymentservice.application.service.PaymentService;
import com.ipho4ticket.paymentservice.domain.model.Payment;
import com.ipho4ticket.paymentservice.domain.model.PaymentMethod;
import com.ipho4ticket.paymentservice.domain.model.PaymentStatus;
import com.ipho4ticket.paymentservice.domain.repository.PaymentRepository;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private Long userId;
    private UUID ticketId;

    @BeforeEach
    void 설정() {
        userId = 1L;
        ticketId = UUID.randomUUID();
        payment = Payment.builder()
            .userId(userId)
            .ticketId(ticketId)
            .amount(BigDecimal.valueOf(100.0))
            .method(PaymentMethod.KAKAOPAY)
            .status(PaymentStatus.OPENED)
            .build();

        // paymentId를 리플렉션으로 설정
        UUID generatedPaymentId = UUID.randomUUID();
        ReflectionUtils.setField(payment, "paymentId", generatedPaymentId);
    }

    @Test
    void 결제_생성_성공() {
        // 결제 요청 DTO 생성
        PaymentRequestDTO requestDTO = new PaymentRequestDTO(
            userId,
            ticketId,
            PaymentMethod.KAKAOPAY,
            100L
        );

        // save() 메소드 모킹
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // 결제 생성 로직 실행
        PaymentResponseDTO response = paymentService.createPayment(requestDTO);

        // save() 메서드 호출 여부 검증
        verify(paymentRepository, times(1)).save(any(Payment.class));  // 메서드 호출 확인
    }

    @Test
    void 결제_단건_조회_성공() throws AccessDeniedException {
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        PaymentResponseDTO response = paymentService.getPayment(payment.getPaymentId(), userId);

        assertNotNull(response);
        assertEquals(payment.getPaymentId(), response.getPaymentId()); // 리플렉션으로 설정한 UUID 확인
        verify(paymentRepository, times(1)).findById(payment.getPaymentId());
    }

    @Test
    void 결제_조회_권한_없음() {
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThrows(AccessDeniedException.class, () -> {
            paymentService.getPayment(payment.getPaymentId(), 2L);  // 다른 userId
        });
    }

    @Test
    void 결제_목록_조회_성공() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(Arrays.asList(payment), pageable, 1);
        when(paymentRepository.findByUserId(userId, pageable)).thenReturn(paymentPage);

        Page<PaymentResponseDTO> responsePage = paymentService.getAllPayments(userId, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(payment.getPaymentId(), responsePage.getContent().get(0).getPaymentId());
        verify(paymentRepository, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void 결제_검색_성공() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> paymentPage = new PageImpl<>(Arrays.asList(payment), pageable, 1);
        when(paymentRepository.searchWithParams(anyMap(), eq(pageable))).thenReturn(paymentPage);

        Page<PaymentResponseDTO> responsePage = paymentService.searchPayments(Map.of("status", "OPENED"), pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(payment.getPaymentId(), responsePage.getContent().get(0).getPaymentId());
        verify(paymentRepository, times(1)).searchWithParams(anyMap(), eq(pageable));
    }

    @Test
    void 결제_취소_성공() throws AccessDeniedException {
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        payment.updateStatus(PaymentStatus.COMPLETED);  // 상태를 COMPLETED로 설정
        PaymentResponseDTO response = paymentService.cancelPayment(payment.getPaymentId(), userId);

        assertNotNull(response);
        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
        verify(paymentRepository, times(1)).findById(payment.getPaymentId());
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void 결제_취소_권한_없음() {
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThrows(AccessDeniedException.class, () -> {
            paymentService.cancelPayment(payment.getPaymentId(), 2L);  // 다른 userId
        });
    }

    @Test
    void 결제_취소_잘못된_상태() {
        payment.updateStatus(PaymentStatus.OPENED);  // 상태가 COMPLETED가 아님
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThrows(IllegalStateException.class, () -> {
            paymentService.cancelPayment(payment.getPaymentId(), userId);
        });
    }
}
