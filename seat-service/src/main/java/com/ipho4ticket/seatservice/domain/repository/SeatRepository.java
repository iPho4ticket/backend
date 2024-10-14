package com.ipho4ticket.seatservice.domain.repository;

import com.ipho4ticket.seatservice.domain.model.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    Page<Seat> findByEventId(UUID eventId, Pageable pageable);

    Seat findBySeatNumberAndEventId(String seatNumber, UUID eventId);
}
