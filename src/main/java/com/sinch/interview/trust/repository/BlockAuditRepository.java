package com.sinch.interview.trust.repository;

import com.sinch.interview.trust.domain.BlockAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlockAuditRepository extends JpaRepository<BlockAudit, UUID> {
    List<BlockAudit> findByProjectIdOrderByCreatedAtDesc(String projectId);
}
