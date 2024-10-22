package com.ipho4ticket.clienteventfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.ipho.common.dto.SeatRequestDto;

@FeignClient(name="seat-service",url="http://seat-service:19094")
public interface ClientSeatFeign{

    @PostMapping("/api/v1/internal/seat")
    SeatRequestDto getSeat(@RequestBody SeatRequestDto request);
}


