package com.sinch.interview.trust.events;

import com.sinch.interview.trust.api.ProjectNotFoundException;
import com.sinch.interview.trust.domain.Project;
import com.sinch.interview.trust.domain.TrustLevel;
import com.sinch.interview.trust.repository.ProjectRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TrustUpdateHandler {

    private final ProjectRepository projectRepository;

    public TrustUpdateHandler(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public void handle(BillingStatusEvent event) {
        Project project = projectRepository.findById(event.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(event.projectId()));

        if (project.getLastEventAt() != null
                && !event.eventTimestamp().isAfter(project.getLastEventAt())) {
            return;
        }

        if (project.isBlocked()) {
            return;
        }

        TrustLevel trustLevel = "ACTIVE".equals(event.accountStatus())
                ? TrustLevel.LIMITED
                : TrustLevel.UNVERIFIED;

        project.setTrustLevel(trustLevel);
        project.setLastEventAt(event.eventTimestamp());
        projectRepository.save(project);
    }
}
