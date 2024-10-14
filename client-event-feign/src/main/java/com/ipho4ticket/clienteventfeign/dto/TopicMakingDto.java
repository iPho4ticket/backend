package com.ipho4ticket.clienteventfeign.dto;

import java.util.UUID;

public record TopicMakingDto(
        String topic,
        UUID eventId
) {
}
