package service.event.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import service.event.domain.model.Event;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID>, QuerydslPredicateExecutor<Event> {
    Page<Event> findAllByIsDeletedFalse(Pageable pageable);

}
