package com.ipho4ticket.eventservice.domain.repository;


import com.ipho4ticket.eventservice.domain.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, QuerydslPredicateExecutor<Event> {
    Page<Event> findAllByIsDeletedFalse(Pageable pageable);

}
