package com.ipho4ticket.seatservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipho4ticket.clienteventfeign.ClientEventFeign;
import com.ipho4ticket.clienteventfeign.dto.EventResponseDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import com.ipho4ticket.seatservice.application.service.EventProducer;
import com.ipho4ticket.seatservice.application.service.SeatService;
import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import com.ipho4ticket.seatservice.domain.repository.SeatRepository;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ClientEventFeign clientEventFeign;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private SeatService seatService;

    private SeatRequestDto request;
    private UUID seatId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        eventId = UUID.randomUUID();
        seatId = UUID.randomUUID();

        // SeatRequestDTO 초기화
        request = new SeatRequestDto(eventId, "A", 12, BigDecimal.valueOf(10000));
    }

    // SeatRequestDTO를 이용해 Seat 객체 생성하는 메서드
    private Seat createSeatFromRequest(UUID seatId, SeatRequestDto request, SeatStatus status) {
        String seatNumber = request.row() + request.column();
        return new Seat(seatId, request.eventId(), seatNumber, status, request.price());
    }

    @Test
    void testCreateSeat() {
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);

        when(seatRepository.save(any(Seat.class))).thenReturn(seat);
        SeatResponseDto createdSeatDTO = seatService.createSeat(request);

        verify(seatRepository, times(1)).save(any(Seat.class));

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertEquals(request.row() + request.column(), createdSeatDTO.getSeatNumber());
    }

//    @Test
//    void testGetAllSeats() {
//        // Pageable 및 Seat 생성
//        Pageable pageable = mock(Pageable.class);
//        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);
//        Page<Seat> seatPage = new PageImpl<>(Collections.singletonList(seat));
//
//        // SeatRepository에서 EventId로 좌석 목록을 반환하도록 설정
//        when(seatRepository.findByEventId(eventId, pageable)).thenReturn(seatPage);
//
//        // RedisTemplate의 opsForValue() 메서드 모킹 및 반환 값 설정
//        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
//        when(redisTemplate.opsForValue()).thenReturn(valueOps);
//
//        // 이벤트 정보를 설정
//        EventResponseDto eventResponse = new EventResponseDto(
//                eventId,
//                "이벤트 제목",
//                "이벤트 설명",
//                LocalDate.now(),
//                LocalDateTime.now(),
//                LocalDateTime.now().plusHours(2)
//        );
//
//        // ClientEventFeign의 getEvent 메서드 모킹
//        when(clientEventFeign.getEvent(eventId)).thenReturn(eventResponse);
//
//        // 좌석이 Redis에 없을 경우를 가정하여 null 반환 설정
//        when(redisTemplate.opsForValue().get("seats")).thenReturn(null);
//
//        // SeatService의 getAllSeats() 메서드 호출
//        Map<String, Object> result = seatService.getAllSeats(eventId, pageable);
//
//        // SeatRepository가 올바르게 호출되었는지 검증
//        verify(seatRepository, times(1)).findByEventId(eventId, pageable);
//
//        // 반환된 결과에서 'seats'를 추출하고 검증
//        Page<SeatResponseDto> seatsPage = (Page<SeatResponseDto>) result.get("seats");
//        SeatResponseDto seatResponseDto = seatsPage.getContent().get(0);
//
//        // 이벤트 정보가 올바르게 반환되었는지 확인
//        EventResponseDto resultEventResponse = (EventResponseDto) result.get("event");
//        assertNotNull(resultEventResponse);
//
//        verify(valueOps, times(1)).set(eq("seat::" + seatId), any(), eq(10L), eq(TimeUnit.SECONDS));
//    }

    @Test
    void testGetSeat() throws JsonProcessingException {
        // 주어진 좌석 ID로 좌석 생성
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);
        String cacheKey = "seat::" + seatId;

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        when(valueOps.get(cacheKey)).thenReturn(seat);

        when(objectMapper.writeValueAsString(seat)).thenReturn("cachedSeatJson");

        SeatResponseDto seatResponseDto = SeatResponseDto.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
        when(objectMapper.readValue("cachedSeatJson", SeatResponseDto.class)).thenReturn(seatResponseDto);

        SeatResponseDto result = seatService.getSeat(seatId);

        assertEquals(seat.getSeatNumber(), result.getSeatNumber());
    }


    @Test
    void testDeleteSeat() {
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        seatService.deleteSeat(seatId);

        verify(seatRepository, times(1)).findById(seatId);
        verify(seatRepository, times(1)).delete(seat);
    }

    @Test
    void testDeleteSeatNotFound() {
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> seatService.deleteSeat(seatId));

        assertEquals(seatId + "는 찾을 수 없는 좌석입니다.", exception.getMessage());

        verify(seatRepository, never()).delete(any(Seat.class));
    }

    @Test
    void testChangeSeatToSold() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.RESERVED);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        seatService.ChangeSeatToSold(seatId);

        assertEquals(SeatStatus.SOLD, seat.getStatus());
        verify(seatRepository, times(1)).save(seat);
    }

    @Test
    void testChangeSeatToAvailable() {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.RESERVED);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        seatService.ChangeSeatToAvailable(seatId);

        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        verify(seatRepository, times(1)).save(seat);
    }
}
