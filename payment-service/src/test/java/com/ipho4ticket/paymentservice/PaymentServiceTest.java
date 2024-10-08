package com.ipho4ticket.paymentservice;

import com.ipho4ticket.clientticketfeign.ClientTicketFeign;
import com.ipho4ticket.clientticketfeign.dto.ValidationResponse;
import com.ipho4ticket.paymentservice.application.dto.ApproveResponse;
import com.ipho4ticket.paymentservice.application.dto.ReadyResponse;
import com.ipho4ticket.paymentservice.application.factory.PaymentProcessorFactory;
import com.ipho4ticket.paymentservice.application.service.PaymentService;
import com.ipho4ticket.paymentservice.domain.model.Payment;
import com.ipho4ticket.paymentservice.domain.model.PaymentMethod;
import com.ipho4ticket.paymentservice.domain.model.PaymentStatus;
import com.ipho4ticket.paymentservice.domain.repository.PaymentRepository;
import com.ipho4ticket.paymentservice.domain.service.PaymentProcessor;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    private ClientTicketFeign clientTicketFeign;  // Feign 클라이언트 모킹
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProcessorFactory paymentProcessorFactory;

    @Mock
    private PaymentProcessor paymentProcessor;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;
    private Long userId;
    private UUID ticketId;
    private ReadyResponse readyResponse;
    private ApproveResponse approveResponse;
    @BeforeEach
    void 설정() {
        userId = 1L;
        ticketId = UUID.randomUUID();
        payment = Payment.builder()
            .userId(userId)
            .ticketId(ticketId)
            .amount(BigDecimal.valueOf(100.0))
            .method(PaymentMethod.KAKAO_PAY)
            .status(PaymentStatus.OPENED)
            .build();

        // paymentId를 리플렉션으로 설정
        UUID generatedPaymentId = UUID.randomUUID();
        ReflectionUtils.setField(payment, "paymentId", generatedPaymentId);

        // 결제 준비 응답
        readyResponse = new ReadyResponse();
        readyResponse.setTid("T123456789");
        readyResponse.setNext_redirect_pc_url("http://redirect-url");

        // 결제 승인 응답
        approveResponse = new ApproveResponse();
        approveResponse.setTid("T123456789");
        approveResponse.setAid("A123456789");

    }

    @Test
    void 결제_요청_성공() {
        // 결제 요청 DTO 생성
        PaymentRequestDTO requestDTO = new PaymentRequestDTO(
            userId,
            ticketId,
            PaymentMethod.KAKAO_PAY,
            100L
        );

        // Feign 클라이언트 모킹 설정
        ValidationResponse validationResponse = new ValidationResponse(true, "Valid ticket");
        when(clientTicketFeign.validateTicket(any(UUID.class), any(Long.class)))
            .thenReturn(validationResponse);

        // 기타 모킹 설정
        when(paymentProcessorFactory.getPaymentProcessor(PaymentMethod.KAKAO_PAY))
            .thenReturn(paymentProcessor);
        when(paymentProcessor.payReady(anyString(), any(BigDecimal.class), any(UUID.class), any()))
            .thenReturn(readyResponse);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // 결제 요청 실행
        ReadyResponse response = paymentService.createPayment(requestDTO);

        // 응답 검증
        assertNotNull(response);
        assertEquals("T123456789", response.getTid());
        assertEquals("http://redirect-url", response.getNext_redirect_pc_url());
    }


    @Test
    void 결제_승인_성공() {
        // 결제 상태를 PENDING으로 설정
        payment.updateStatus(PaymentStatus.PENDING);

        // TID 값을 설정
        payment.setTid("T123456789");

        // 결제 상태가 PENDING인 결제를 찾는 부분 Mock 처리
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        // 결제 승인 처리 (payApprove 호출 인자 값 정확하게 설정)
        when(paymentProcessorFactory.getPaymentProcessor(PaymentMethod.KAKAO_PAY))
            .thenReturn(paymentProcessor);
        when(paymentProcessor.payApprove(eq("T123456789"), eq("pgTokenSample")))
            .thenReturn(approveResponse);

        // 결제 승인 실행
        PaymentResponseDTO response = paymentService.approvePayment(payment.getPaymentId(), "pgTokenSample");

        // 결제 승인 결과 확인
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());

        // 결제 저장 확인
        verify(paymentRepository, times(1)).save(any(Payment.class));  // 승인 후 두 번 저장
        verify(paymentProcessor, times(1)).payApprove(eq("T123456789"), eq("pgTokenSample"));
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
