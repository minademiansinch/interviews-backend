package com.sinch.interview.trust.repository;

import com.sinch.interview.trust.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByAggregateIdOrderByCreatedAtAsc(String aggregateId);
}
