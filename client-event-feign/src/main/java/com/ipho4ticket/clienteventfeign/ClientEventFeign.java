package com.ipho4ticket.clienteventfeign;

import com.ipho4ticket.clienteventfeign.dto.EventResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name="event-service",url="http://event-service:19093")
public interface ClientEventFeign{

    @GetMapping("/api/v1/internal/event/{event_id}")
    EventResponseDto getEvent(@PathVariable("event_id") UUID eventId);
}


