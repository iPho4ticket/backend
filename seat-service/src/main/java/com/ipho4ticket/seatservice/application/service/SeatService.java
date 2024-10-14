package com.ipho4ticket.seatservice.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipho4ticket.clienteventfeign.ClientEventFeign;
import com.ipho4ticket.seatservice.application.config.ConcurrencyControl;
import com.ipho4ticket.seatservice.application.events.SeatBookingEvent;
import com.ipho4ticket.seatservice.application.events.TicketMakingEvent;
import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import com.ipho4ticket.seatservice.domain.repository.SeatRepository;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ipho4ticket.seatservice.domain.model.QSeat.seat;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ClientEventFeign clientEventFeign;
    private final EventProducer eventProducer;

    @Transactional
    public SeatResponseDto createSeat(@Valid SeatRequestDto request) {
        // 행 값+열 값 -> 좌석 생성
        Seat seat=new Seat(request.eventId(),request.row(),request.column(),request.price());

        if (seatRepository.findBySeatNumberAndEventId(seat.getSeatNumber(), seat.getEventId())) {
            throw new IllegalArgumentException(seat.getSeatNumber() + "는 이미 등록된 좌석입니다.");
        }
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
    public Map<String, Object> getAllSeats(UUID eventId, Pageable pageable) {

        // 관련 event 조회
        // EventResponseDto event=clientEventFeign.getEvent(eventId);

        // Redis에서 데이터 조회
        Page<SeatResponseDto> seatsPage;

        List<SeatResponseDto> cachedSeats = (List<SeatResponseDto>) redisTemplate.opsForValue().get("seats"); // 'seats' 키로 데이터를 가져옴

        // Redis에 캐시된 데이터가 있는지 확인
        if (cachedSeats != null) {
            seatsPage=new PageImpl<>(cachedSeats, pageable, cachedSeats.size()); // 캐시된 데이터가 있으면 반환
        }else{
            // 데이터베이스에서 좌석 정보를 조회
            Page<Seat> seats = seatRepository.findByEventId(eventId, pageable);

            // 좌석 정보를 DTO로 변환
            List<SeatResponseDto> seatResponseDtos = seats.stream()
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());

            cacheSeats(seatResponseDtos);

            seatsPage=new PageImpl<>(seatResponseDtos, pageable, seatResponseDtos.size());
        }
        // 이벤트와 좌석 정보를 Map으로 반환
        // "event", event,
        return Map.of("seats", seatsPage);
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


    // 좌석 삭제
    @Transactional
    public void deleteSeat(UUID seatId) {
        Seat seat=seatRepository.findById(seatId)
                .orElseThrow(()->new IllegalArgumentException(seatId+"는 찾을 수 없는 좌석입니다."));
        seatRepository.delete(seat);
    }

    /**
     * 좌석 예약 요청 -> 좌석 체크 후 감소 -> 티켓 생성 요청
     * 이 과정은 동기적으로 처리해야되지 않을까,,,
     */
    @Transactional
    public void checkSeat(SeatBookingEvent request) {
        Seat seat=getSeatInfo(request.getSeatId());

        if (seat.getStatus().equals(SeatStatus.AVAILABLE)){
            // 구매 가능하다면 상태 변경
            updateSeatStatus(seat);
            eventProducer.publishTicketMakingEvent(new TicketMakingEvent(request.getTicketId(), request.getSeatId(), request.getSeatNumber(),seat.getPrice()));
        }
    }

    @ConcurrencyControl(lockName = "ChangeSeatToReserved")
    public void updateSeatStatus(Seat seat){
        seat.updateStatus(SeatStatus.RESERVED); // 좌석 상태 업데이트

        /**
         * DB업데이트 + Redis 업데이트
         * - 분산 트랜잭션
         * - DB와 redis 둘 다 수정 : Write-Through 전략
         */
        seatRepository.save(seat); // 상태 업데이트 후 좌석 저장
        redisTemplate.opsForValue().set(seat.getId().toString(), seat);  // 캐시에 상태 업데이트
    }




    public Seat getSeatInfo(UUID seatId){
        // Redis에서 좌석 데이터 조회
        String cacheKey = "seat::" + seatId; // 캐시 키 설정
        Object cachedSeat = redisTemplate.opsForValue().get(cacheKey);

        // 좌석 데이터를 캐시에서 가져오거나 DB에서 조회
        Seat seat = Optional.ofNullable(cachedSeat)
                .map(seatObj -> objectMapper.convertValue(seatObj, Seat.class)) // 캐시에서 가져온 데이터를 Seat 객체로 변환
                .orElseGet(() -> seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException(seatId + "는 찾을 수 없는 좌석입니다.")));
        return seat;
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
