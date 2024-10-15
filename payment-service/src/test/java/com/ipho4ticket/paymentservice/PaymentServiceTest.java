package com.ipho4ticket.paymentservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.ipho4ticket.paymentservice.infrastructure.external.KakaoPayService;
import com.ipho4ticket.paymentservice.presentation.request.PaymentRequestDTO;
import com.ipho4ticket.paymentservice.presentation.response.PaymentResponseDTO;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private KakaoPayService kakaoPayService;

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
    void 결제_승인_성공_티켓_상태_변경_재시도_및_취소() {
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

        // Mock the response of clientTicketFeign.changeTicketStatus to return a successful ValidationResponse
        ValidationResponse validationResponse = new ValidationResponse(true, "Success");
        when(clientTicketFeign.changeTicketStatus(ticketId)).thenReturn(validationResponse);

        // 결제 승인 실행
        PaymentResponseDTO response = paymentService.approvePayment(payment.getPaymentId(),
            ticketId, "pgTokenSample");

        // 결제 승인 결과 확인
        assertNotNull(response);
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());

        // 결제 저장 확인
        verify(paymentRepository, times(1)).save(any(Payment.class));  // 승인 후 두 번 저장
        verify(paymentProcessor, times(1)).payApprove(eq("T123456789"), eq("pgTokenSample"));
        verify(clientTicketFeign, times(1)).changeTicketStatus(ticketId);  // 티켓 상태 변경 호출 확인
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

        Page<PaymentResponseDTO> responsePage = paymentService.searchPayments(
            Map.of("status", "OPENED"), pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals(payment.getPaymentId(), responsePage.getContent().get(0).getPaymentId());
        verify(paymentRepository, times(1)).searchWithParams(anyMap(), eq(pageable));
    }


    @Test
    void 결제_취소_성공() throws AccessDeniedException {
        // Mocking 결제 데이터
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        // 상태를 COMPLETED로 설정
        payment.updateStatus(PaymentStatus.COMPLETED);

        // UUID로 티켓 ID 설정
        UUID ticketId = UUID.randomUUID();
        ReflectionUtils.setField(payment, "ticketId", ticketId);

        // Mocking 티켓 상태 검증 응답
        ValidationResponse validationResponse = new ValidationResponse(true, "Valid");
        when(clientTicketFeign.checkTicketCancel(eq(ticketId), eq(payment.getUserId()))).thenReturn(
            validationResponse);

        // Mocking 결제 취소 호출
        ApproveResponse approveResponse = new ApproveResponse();
        approveResponse.setTid("T123456789");
        approveResponse.setAid("A123456789");
        when(paymentProcessorFactory.getPaymentProcessor(PaymentMethod.KAKAO_PAY)).thenReturn(
            paymentProcessor);
        when(paymentProcessor.cancelPayment(eq("T123456789"), eq(10000), eq(0), eq(909)))
            .thenReturn(approveResponse);

        // Mocking 티켓 상태 변경 응답
        ValidationResponse validationPostResponse = new ValidationResponse(true, "Status changed");
        when(clientTicketFeign.changeTicketStatusCancel(ticketId)).thenReturn(validationPostResponse);

        // 결제 취소 실행
        ApproveResponse response = paymentService.cancelPayment(payment.getPaymentId(), userId,
            "T123456789", 10000, 0, 909);

        // 검증
        assertNotNull(response);
        assertEquals("T123456789", response.getTid());
        assertEquals("A123456789", response.getAid());
        assertEquals(PaymentStatus.CANCELED, payment.getStatus());

        // 검증 - 결제 상태 업데이트 및 저장 호출
        verify(paymentRepository, times(1)).findById(payment.getPaymentId());
        verify(paymentRepository, times(1)).save(payment);
        verify(paymentProcessor, times(1)).cancelPayment(eq("T123456789"), eq(10000), eq(0),
            eq(909));

        // 티켓 검증 및 상태 변경 호출 확인
        verify(clientTicketFeign, times(1)).checkTicketCancel(eq(ticketId),
            eq(payment.getUserId()));
        verify(clientTicketFeign, times(1)).changeTicketStatusCancel(ticketId);
    }


    @Test
    void 결제_취소_권한_없음() {
        // Mocking 결제 정보
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        // UUID로 티켓 ID 설정 (Reflection 사용)
        UUID ticketId = UUID.randomUUID();
        ReflectionTestUtils.setField(payment, "ticketId", ticketId);  // Reflection 사용하여 필드 설정

        // 다른 사용자의 결제 취소 시도
        assertThrows(AccessDeniedException.class, () -> {
            paymentService.cancelPayment(payment.getPaymentId(), 2L, "T123456789", 10000, 0,
                909);  // 다른 userId로 취소 시도
        });

        // 검증 - 결제 정보 조회만 발생하고 취소 로직은 실행되지 않음
        verify(paymentRepository, times(1)).findById(payment.getPaymentId());
        verify(paymentProcessor, times(0)).cancelPayment(anyString(), anyInt(), anyInt(),
            anyInt());  // 취소 호출이 발생하지 않아야 함
        verify(clientTicketFeign, times(0)).checkTicketCancel(any(UUID.class),
            anyLong());  // 티켓 검증이 발생하지 않아야 함
    }


    @Test
    void 결제_취소_잘못된_상태() {
        // Mocking 결제 정보
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        // UUID로 티켓 ID 설정 (Reflection 사용)
        UUID ticketId = UUID.randomUUID();
        ReflectionTestUtils.setField(payment, "ticketId", ticketId);  // Reflection 사용하여 필드 설정

        // 결제 상태가 COMPLETED가 아닌 경우 (예: OPENED 상태)
        payment.updateStatus(PaymentStatus.OPENED);

        // 결제 상태가 잘못된 경우 IllegalStateException 발생 확인
        assertThrows(IllegalStateException.class, () -> {
            paymentService.cancelPayment(payment.getPaymentId(), userId, "T123456789", 10000, 0,
                909);
        });

        // 검증 - 결제 정보 조회만 발생하고 취소 로직은 실행되지 않음
        verify(paymentRepository, times(1)).findById(payment.getPaymentId());
        verify(paymentProcessor, times(0)).cancelPayment(anyString(), anyInt(), anyInt(),
            anyInt());  // 취소 호출이 발생하지 않아야 함
        verify(clientTicketFeign, times(0)).checkTicketCancel(any(UUID.class),
            anyLong());  // 티켓 검증이 발생하지 않아야 함
    }


}
