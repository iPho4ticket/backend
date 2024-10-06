package com.ipho4ticket.seatservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipho4ticket.clienteventfeign.dto.EventResponseDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

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

        // 좌석 상태가 AVAILABLE로 변경되었는지 확인
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());

        // 생성된 SeatResponseDTO의 속성이 올바르게 설정되었는지 확인
        assertEquals(request.row() + request.column(), createdSeatDTO.getSeatNumber());
        assertEquals(request.price(), createdSeatDTO.getPrice());
        assertEquals(SeatStatus.AVAILABLE, createdSeatDTO.getStatus());
    }

    @Test
    void testGetAllSeats() {
        // Pageable 및 Seat 생성
        Pageable pageable = mock(Pageable.class);
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);
        Page<Seat> seatPage = new PageImpl<>(Collections.singletonList(seat));

        // SeatRepository에서 EventId로 좌석 목록을 반환하도록 설정
        when(seatRepository.findByEventId(eventId, pageable)).thenReturn(seatPage);

        // RedisTemplate의 opsForValue() 메서드 모킹 및 반환 값 설정
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // SeatService의 getAllSeats() 메서드 호출
        Map<String, Object> result = seatService.getAllSeats(eventId, pageable);

        // SeatRepository가 올바르게 호출되었는지 검증
        verify(seatRepository, times(1)).findByEventId(eventId, pageable);

        // 반환된 결과에서 'seats'를 추출하고 검증
        Page<SeatResponseDto> seatsPage = (Page<SeatResponseDto>) result.get("seats");
        SeatResponseDto seatResponseDto = seatsPage.getContent().get(0);

        // 좌석 정보가 올바르게 매핑되었는지 확인
        assertEquals(seat.getSeatNumber(), seatResponseDto.getSeatNumber());
        assertEquals(seat.getPrice(), seatResponseDto.getPrice());
        assertEquals(seat.getStatus(), seatResponseDto.getStatus());

        // 이벤트 정보가 올바르게 반환되었는지 확인 (필요 시 추가)
        EventResponseDto eventResponse = (EventResponseDto) result.get("event");
        assertNotNull(eventResponse);
    }

    @Test
    void testGetSeat() throws JsonProcessingException {
        // 주어진 좌석 ID로 좌석 생성
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);
        String cacheKey = "seat::" + seatId;

        // RedisTemplate의 opsForValue() 메서드가 호출될 경우의 리턴 값 설정
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // 캐시에서 좌석을 반환하도록 설정
        when(valueOps.get(cacheKey)).thenReturn(seat);

        // ObjectMapper의 동작 설정 (직렬화와 역직렬화)
        when(objectMapper.writeValueAsString(seat)).thenReturn("cachedSeatJson");

        // SeatResponseDto 객체를 빌더 패턴을 사용하여 생성
        SeatResponseDto seatResponseDto = SeatResponseDto.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();

        // JSON 문자열을 SeatResponseDto로 변환할 때 반환하도록 설정
        when(objectMapper.readValue("cachedSeatJson", SeatResponseDto.class)).thenReturn(seatResponseDto);

        SeatResponseDto result = seatService.getSeat(seatId);

        verify(redisTemplate.opsForValue(), times(1)).get(cacheKey); // 캐시에서 호출된 횟수 검증
        verify(seatRepository, never()).findAll(); // 캐시 적중 시 findAll 호출되지 않아야 함

        assertEquals(seat.getSeatNumber(), result.getSeatNumber());
        assertEquals(seat.getPrice(), result.getPrice());
        assertEquals(seat.getStatus(), result.getStatus());
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
}
