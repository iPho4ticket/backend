package com.ipho4ticket.seatservice.application.service;

import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import com.ipho4ticket.seatservice.domain.repository.SeatRepository;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    @Transactional
    public SeatResponseDto createSeat(@Valid SeatRequestDto request) {
        // 행 값+열 값 -> 좌석 생성
        Seat seat=Seat.create(request);

        // 좌석 상태 변경 - 판매가능
        seat.updateStatus(SeatStatus.AVAILABLE);

        seatRepository.save(seat);
        return toResponseDTO(seat);
    }

    public Page<SeatResponseDto> getAllSeats(UUID eventId, Pageable pageable) {
        Page<Seat> seats=seatRepository.findByEventId(eventId,pageable);
        return seats.map(this::toResponseDTO);
    }

    public SeatResponseDto getSeat(UUID seatId) {
        Seat seat= seatRepository.findById(seatId)
                .orElseThrow(()->new IllegalArgumentException(seatId+"는 찾을 수 없는 좌석입니다."));
        return toResponseDTO(seat);
    }

    @Transactional
    public void deleteSeat(UUID seatId) {
        Seat seat=seatRepository.findById(seatId)
                .orElseThrow(()->new IllegalArgumentException(seatId+"는 찾을 수 없는 좌석입니다."));
        seatRepository.delete(seat);
    }

    private SeatResponseDto toResponseDTO(Seat seat){
        return SeatResponseDto.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
    }
}
