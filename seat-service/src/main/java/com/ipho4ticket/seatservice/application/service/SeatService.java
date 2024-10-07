package com.ipho4ticket.seatservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import com.ipho4ticket.seatservice.domain.repository.SeatRepository;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public SeatResponseDto createSeat(@Valid SeatRequestDto request) {
        // 행 값+열 값 -> 좌석 생성
        Seat seat=Seat.create(request);

        // 좌석 상태 변경 - 판매가능
        seat.updateStatus(SeatStatus.AVAILABLE);

        seatRepository.save(seat);
        return toResponseDTO(seat);
    }

    /**
     * 좌석 전체 조회 시(seatId가 key),
     * 1. redis에 데이터가 없다면 -> TTL 초과되었다는 뜻 -> 전체 좌석 다시 저장
     * 2. redis에 데이터가 있다면 -> redis에서 호출
     */
    public Page<SeatResponseDto> getAllSeats(UUID eventId, Pageable pageable) {
        // Redis에서 데이터 조회
        List<SeatResponseDto> cachedSeats = (List<SeatResponseDto>) redisTemplate.opsForValue().get("seats"); // 'seats' 키로 데이터를 가져옴

        // Redis에 캐시된 데이터가 있는지 확인
        if (cachedSeats != null) {
            return new PageImpl<>(cachedSeats, pageable, cachedSeats.size()); // 캐시된 데이터가 있으면 반환
        }

        // 데이터베이스에서 좌석 정보를 조회
        Page<Seat> seats = seatRepository.findByEventId(eventId, pageable);

        // 좌석 정보를 DTO로 변환
        List<SeatResponseDto> seatResponseDtos = seats.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        cacheSeats(seatResponseDtos);

        return new PageImpl<>(seatResponseDtos, pageable, seatResponseDtos.size());
    }


    /**
     * 좌석 단건 조회 시 (seatId가 key),
     * 1. 좌석이 redis에 없다면 -> TTL 초과되었다는 뜻 -> 전체 좌석 다시 저장
     * 2. 좌석이 redis에 있다면 -> redis에서 호출
     */
    @SneakyThrows
    public SeatResponseDto getSeat(UUID seatId) {
        // Redis에서 좌석 데이터 조회
        String cacheKey = "seat::" + seatId; // 캐시 키 설정
        Object cachedSeat = redisTemplate.opsForValue().get(cacheKey);

        String cachedSeatJson = objectMapper.writeValueAsString(cachedSeat); // LinkedHashMap을 JSON 문자열로 변환

        if (cachedSeatJson != null) {
            return objectMapper.readValue(cachedSeatJson, SeatResponseDto.class); // JSON 문자열을 DTO로 변환하여 반환
        }

        List<Seat> seats = seatRepository.findAll();

        // 좌석 정보를 DTO로 변환
        List<SeatResponseDto> seatResponseDtos = seats.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        cacheSeats(seatResponseDtos);

        return (SeatResponseDto) redisTemplate.opsForValue().get(seatId);
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

    private void cacheSeats(List<SeatResponseDto> seatResponseDtos){
        String cacheKey;
        // 각 좌석 정보를 Redis에 저장
        for (SeatResponseDto seatResponseDto : seatResponseDtos) {
            cacheKey = "seat::" + seatResponseDto.getSeatId();
            // TTL = 10s
            redisTemplate.opsForValue().set(cacheKey, seatResponseDto,10,TimeUnit.SECONDS);
        }
    }
}
