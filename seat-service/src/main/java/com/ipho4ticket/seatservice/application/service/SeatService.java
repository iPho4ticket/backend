package com.ipho4ticket.seatservice.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipho.common.dto.CancelTicketEvent;
import com.ipho.common.dto.ConfirmSeatEvent;
import com.ipho.common.dto.SeatBookingEvent;
import com.ipho.common.dto.TicketMakingEvent;
import com.ipho4ticket.clienteventfeign.ClientEventFeign;
import com.ipho4ticket.clienteventfeign.dto.EventResponseDto;
import com.ipho4ticket.seatservice.application.config.ConcurrencyControl;
import com.ipho4ticket.seatservice.application.service.exception.EventNotExistsException;
import com.ipho4ticket.seatservice.application.service.exception.SeatAlreadyExistsException;
import com.ipho4ticket.seatservice.application.service.exception.SeatNotExistsException;
import com.ipho4ticket.seatservice.domain.model.Seat;
import com.ipho4ticket.seatservice.domain.model.SeatStatus;
import com.ipho4ticket.seatservice.domain.repository.SeatRepository;
import com.ipho4ticket.seatservice.infra.TicketClientService;
import com.ipho4ticket.seatservice.presentation.request.SeatRequestDto;
import com.ipho4ticket.seatservice.application.dto.SeatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ClientEventFeign clientEventFeign;
    private final EventProducer eventProducer;
    private final TicketClientService ticketClientService;

    @Transactional
    public SeatResponseDto createSeat(SeatRequestDto request) {
        // 행 값+열 값 -> 좌석 생성
        Seat seat=new Seat(request.eventId(),request.row(),request.column(),request.price());

        if (seatRepository.findBySeatNumberAndEventId(seat.getSeatNumber(), seat.getEventId())!=null) {
            throw new SeatAlreadyExistsException(seat.getSeatNumber() + "는 이미 등록된 좌석입니다.");
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
    public Page<SeatResponseDto> getAllSeats(UUID eventId, Pageable pageable) {
        Page<SeatResponseDto> seatsPage;

        // 관련 event 조회
        EventResponseDto event = Optional.ofNullable(clientEventFeign.getEvent(eventId))
                .orElseThrow(() -> new EventNotExistsException("해당 공연 정보가 없습니다."));


        // Redis에서 데이터 조회
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
        return seatsPage;
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

        // 좌석 데이터를 캐시에서 가져오거나 DB에서 조회
        Seat seat = Optional.ofNullable(cachedSeat)
                .map(seatObj -> objectMapper.convertValue(seatObj, Seat.class)) // 캐시에서 가져온 데이터를 Seat 객체로 변환
                .orElseGet(() -> {
                    // 좌석 정보를 DTO로 변환
                    List<SeatResponseDto> seatResponse = seatRepository.findAll().stream()
                            .map(this::toResponseDTO)
                            .collect(Collectors.toList());

                    cacheSeats(seatResponse);
                    return seatRepository.findById(seatId).orElseThrow(() -> new SeatNotExistsException(seatId + "는 찾을 수 없는 좌석입니다."));
                });

        return toResponseDTO(seat);
    }


    @Transactional
    public void deleteSeat(UUID seatId) {
        Seat seat=seatRepository.findById(seatId)
                .orElseThrow(()->new SeatNotExistsException(seatId+"는 찾을 수 없는 좌석입니다."));
        seatRepository.delete(seat);
    }

    /**
     * 좌석 예약 요청 -> 좌석 체크 후 감소 -> 티켓 생성 요청
     */
    @Transactional
    public void checkSeat(SeatBookingEvent request) {
        EventResponseDto event=clientEventFeign.getEvent(request.getEventId());

        Seat seat = Optional.ofNullable(seatRepository.findBySeatNumberAndEventId(request.getSeatNumber(), request.getEventId()))
                .orElseThrow(() -> new SeatNotExistsException(request.getSeatNumber() + "는 찾을 수 없는 좌석입니다."));

        if (seat.getStatus().equals(SeatStatus.AVAILABLE)) {
            // 구매 가능하다면 상태 변경
            updateSeatToReserved(seat);
            ticketClientService.requestRegisterTopic("ticket-making", request.getEventId()).subscribe();
            eventProducer.publishTicketMakingEvent(new TicketMakingEvent(request.getEventId(), request.getTicketId(),seat.getId(),event.title(),request.getSeatNumber(),seat.getPrice()));
        }
    }

    @ConcurrencyControl(lockName = "ChangeSeatToReserved")
    public void updateSeatToReserved(Seat seat){
        seat.updateStatus(SeatStatus.RESERVED); // 좌석 상태 업데이트

        /**
         * DB업데이트 + Redis 업데이트
         * - 분산 트랜잭션
         * - DB와 redis 둘 다 수정 : Write-Through 전략으로 비동기적 저장
         */
        seatRepository.save(seat); // 상태 업데이트 후 좌석 저장
        cacheSeat(toResponseDTO(seat)); // 캐시에 상태 업데이트
    }

    @Transactional
    public void updateSeatToAvailable(CancelTicketEvent request){
        Seat seat = seatRepository.findBySeatNumberAndEventId(request.getSeatNumber(), request.getEventId());
        seat.updateStatus(SeatStatus.AVAILABLE); // 좌석 상태 업데이트

        seatRepository.save(seat); // 상태 업데이트 후 좌석 저장
        cacheSeat(toResponseDTO(seat));
    }

    @Transactional
    public void updateSeatToSold(ConfirmSeatEvent request){
        Seat seat = seatRepository.findBySeatNumberAndEventId(request.getSeatNumber(), request.getEventId());
        seat.updateStatus(SeatStatus.SOLD);

        seatRepository.save(seat); // 상태 업데이트 후 좌석 저장
        cacheSeat(toResponseDTO(seat));
    }


    private SeatResponseDto toResponseDTO(Seat seat){
        return SeatResponseDto.builder()
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
    }

    private void cacheSeats(List<SeatResponseDto> seatResponseDto){
        String cacheKey;
        // 각 좌석 정보를 Redis에 저장
        for (SeatResponseDto seat : seatResponseDto) {
            cacheKey = "seat::" + seat.getSeatId();
            // TTL = 10s
            redisTemplate.opsForValue().set(cacheKey, seat,30,TimeUnit.SECONDS);
        }
    }
    private void cacheSeat(SeatResponseDto seatResponseDto){
        String cacheKey = "seat::" + seatResponseDto.getSeatId();
        // TTL = 10s
        redisTemplate.opsForValue().set(cacheKey, seatResponseDto, 30, TimeUnit.SECONDS);
    }
}
