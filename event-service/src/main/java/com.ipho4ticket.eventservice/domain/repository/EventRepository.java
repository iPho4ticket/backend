package com.ipho4ticket.eventservice.domain.repository;


import com.ipho4ticket.eventservice.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, QuerydslPredicateExecutor<Event> {
}
