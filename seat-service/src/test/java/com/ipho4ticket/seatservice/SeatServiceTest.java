package com.ipho4ticket.seatservice;

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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

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
        return new Seat(seatId, request.eventID(), seatNumber, status, request.price());
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

        Page<SeatResponseDto> result = seatService.getAllSeats(eventId, pageable);

        verify(seatRepository, times(1)).findByEventId(eventId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(seat.getSeatNumber(), result.getContent().get(0).getSeatNumber());
        assertEquals(seat.getPrice(), result.getContent().get(0).getPrice());
        assertEquals(seat.getStatus(), result.getContent().get(0).getStatus());
    }

    @Test
    void testGetSeat() {
        Seat seat = createSeatFromRequest(seatId, request, SeatStatus.AVAILABLE);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        SeatResponseDto result = seatService.getSeat(seatId);

        verify(seatRepository, times(1)).findById(seatId);

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
