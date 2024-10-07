package com.ipho4ticket.seatservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipho4ticket.clienteventfeign.ClientEventFeign;
import com.ipho4ticket.clienteventfeign.dto.EventResponseDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import com.ipho4ticket.seatservice.application.service.SeatService;
import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import com.ipho4ticket.seatservice.domain.repository.SeatRepository;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SeatServiceTest {

    @MockBean
    private SeatRepository seatRepository;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private ClientEventFeign clientEventFeign;

    @Autowired
    private SeatService seatService;

    private SeatRequestDto request;
    private UUID seatId;
    private UUID eventId;
    private LocalDate date=LocalDate.now();
    private LocalDateTime startTime = LocalDateTime.now(); // Set the start time to now
    private LocalDateTime endTime = startTime.plusHours(2); // Example: end time is 2 hours after start time

    private EventResponseDto eventResponseDto;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        seatId = UUID.randomUUID();

        // clientEventFeign 모킹 설정
        eventResponseDto = new EventResponseDto(eventId, "Event Name", "Location",date,startTime,endTime);
        when(clientEventFeign.getEvent(eventId)).thenReturn(eventResponseDto);

        // SeatRequestDTO 초기화
        request = new SeatRequestDto(eventId, "A", 12, BigDecimal.valueOf(10000));
    }

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
        Pageable pageable = mock(Pageable.class);
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);
        Page<Seat> seatPage = new PageImpl<>(Collections.singletonList(seat));

        when(seatRepository.findByEventId(eventId, pageable)).thenReturn(seatPage);

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        Map<String, Object> result = seatService.getAllSeats(eventId, pageable);

        verify(seatRepository, times(1)).findByEventId(eventId, pageable);

        Page<SeatResponseDto> seatsPage = (Page<SeatResponseDto>) result.get("seats");
        SeatResponseDto seatResponseDto = seatsPage.getContent().get(0);

        assertEquals(seat.getSeatNumber(), seatResponseDto.getSeatNumber());
        assertEquals(seat.getPrice(), seatResponseDto.getPrice());
        assertEquals(seat.getStatus(), seatResponseDto.getStatus());

        EventResponseDto eventResponse = (EventResponseDto) result.get("event");
        assertNotNull(eventResponse);
    }

    @Test
    void testGetSeat() throws JsonProcessingException {
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

        verify(redisTemplate.opsForValue(), times(1)).get(cacheKey);

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
