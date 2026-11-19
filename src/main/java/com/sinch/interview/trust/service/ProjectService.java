package com.sinch.interview.trust.service;

import com.sinch.interview.trust.api.FailedPreconditionException;
import com.sinch.interview.trust.api.ProjectNotFoundException;
import com.sinch.interview.trust.api.ProjectResponse;
import com.sinch.interview.trust.domain.AccountType;
import com.sinch.interview.trust.domain.AuditAction;
import com.sinch.interview.trust.domain.BlockAudit;
import com.sinch.interview.trust.domain.OutboxEvent;
import com.sinch.interview.trust.domain.OutboxTarget;
import com.sinch.interview.trust.domain.Project;
import com.sinch.interview.trust.domain.TrustLevel;
import com.sinch.interview.trust.repository.BlockAuditRepository;
import com.sinch.interview.trust.repository.OutboxEventRepository;
import com.sinch.interview.trust.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private static final String PROJECT_BLOCKED_EVENT_TYPE = "ProjectBlocked";

    private final ProjectRepository projectRepository;
    private final BlockAuditRepository blockAuditRepository;
    private final OutboxEventRepository outboxEventRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            BlockAuditRepository blockAuditRepository,
            OutboxEventRepository outboxEventRepository
    ) {
        this.projectRepository = projectRepository;
        this.blockAuditRepository = blockAuditRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public ProjectResponse blockProject(String projectId, String reason, String actor) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getTrustLevel() == TrustLevel.EXEMPT
                || project.getAccountType() == AccountType.ENTERPRISE) {
            throw new FailedPreconditionException(
                    "Project " + projectId + " is exempt and cannot be blocked"
            );
        }

        if (project.isBlocked()) {
            throw new FailedPreconditionException(
                    "Project " + projectId + " is already blocked"
            );
        }

        TrustLevel previousTrustLevel = project.getTrustLevel();
        project.setBlocked(true);
        project.setTrustLevel(TrustLevel.NON_COMPLIANT);
        projectRepository.save(project);

        blockAuditRepository.save(new BlockAudit(
                projectId,
                AuditAction.BLOCK,
                actor,
                reason,
                previousTrustLevel,
                TrustLevel.NON_COMPLIANT
        ));

        String payload = "{\"projectId\":\"" + projectId + "\",\"reason\":\"" + reason + "\"}";
        outboxEventRepository.save(new OutboxEvent(
                projectId,
                OutboxTarget.GATEWAY_CACHE,
                PROJECT_BLOCKED_EVENT_TYPE,
                payload
        ));
        outboxEventRepository.save(new OutboxEvent(
                projectId,
                OutboxTarget.COMPLIANCE_REPORT,
                PROJECT_BLOCKED_EVENT_TYPE,
                payload
        ));

        return ProjectResponse.from(project);
    }
}
