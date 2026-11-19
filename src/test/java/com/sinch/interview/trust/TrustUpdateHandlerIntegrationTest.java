package com.sinch.interview.trust;

import com.sinch.interview.trust.domain.TrustLevel;
import com.sinch.interview.trust.events.BillingStatusEvent;
import com.sinch.interview.trust.events.TrustUpdateHandler;
import com.sinch.interview.trust.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class TrustUpdateHandlerIntegrationTest {

    @Autowired
    TrustUpdateHandler trustUpdateHandler;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    MockMvc mockMvc;

    @Test
    void recomputesActiveToLimitedAndAdvancesLastEventAt() {
        var project = projectRepository.findById("proj-cloud-unverified").orElseThrow();
        Instant previousLastEventAt = project.getLastEventAt();
        Instant eventTimestamp = previousLastEventAt.plusSeconds(60);

        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-cloud-unverified",
                "ACTIVE",
                eventTimestamp
        ));

        var updated = projectRepository.findById("proj-cloud-unverified").orElseThrow();
        assertThat(updated.getTrustLevel()).isEqualTo(TrustLevel.LIMITED);
        assertThat(updated.getLastEventAt()).isEqualTo(eventTimestamp);
        assertThat(updated.isBlocked()).isFalse();
    }

    @Test
    void recomputesNonActiveToUnverified() {
        var project = projectRepository.findById("proj-cloud-trusted").orElseThrow();
        Instant eventTimestamp = project.getLastEventAt().plusSeconds(120);

        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-cloud-trusted",
                "SUSPENDED",
                eventTimestamp
        ));

        var updated = projectRepository.findById("proj-cloud-trusted").orElseThrow();
        assertThat(updated.getTrustLevel()).isEqualTo(TrustLevel.UNVERIFIED);
        assertThat(updated.getLastEventAt()).isEqualTo(eventTimestamp);
    }

    @Test
    void ignoresStaleEventsOlderOrEqualToLastEventAt() {
        var project = projectRepository.findById("proj-cloud-limited").orElseThrow();
        Instant lastEventAt = project.getLastEventAt();
        TrustLevel originalLevel = project.getTrustLevel();

        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-cloud-limited",
                "SUSPENDED",
                lastEventAt.minusSeconds(30)
        ));
        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-cloud-limited",
                "SUSPENDED",
                lastEventAt
        ));

        var unchanged = projectRepository.findById("proj-cloud-limited").orElseThrow();
        assertThat(unchanged.getTrustLevel()).isEqualTo(originalLevel);
        assertThat(unchanged.getLastEventAt()).isEqualTo(lastEventAt);
    }

    @Test
    void blockedProjectIgnoresOlderAndNewerBillingEvents() throws Exception {
        mockMvc.perform(post("/v1/projects/proj-high-volume/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Fraud campaign","actor":"compliance@sinch.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(true))
                .andExpect(jsonPath("$.trustLevel").value("NON_COMPLIANT"));

        var blocked = projectRepository.findById("proj-high-volume").orElseThrow();
        Instant lastEventAt = blocked.getLastEventAt();

        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-high-volume",
                "ACTIVE",
                lastEventAt.minusSeconds(10)
        ));
        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-high-volume",
                "ACTIVE",
                lastEventAt.plusSeconds(3600)
        ));

        var stillBlocked = projectRepository.findById("proj-high-volume").orElseThrow();
        assertThat(stillBlocked.isBlocked()).isTrue();
        assertThat(stillBlocked.getTrustLevel()).isEqualTo(TrustLevel.NON_COMPLIANT);
    }

    @Test
    void alreadyBlockedProjectRemainsNonCompliantOnActiveEvent() {
        var project = projectRepository.findById("proj-already-blocked").orElseThrow();
        Instant eventTimestamp = project.getLastEventAt().plusSeconds(300);

        trustUpdateHandler.handle(new BillingStatusEvent(
                "proj-already-blocked",
                "ACTIVE",
                eventTimestamp
        ));

        var stillBlocked = projectRepository.findById("proj-already-blocked").orElseThrow();
        assertThat(stillBlocked.isBlocked()).isTrue();
        assertThat(stillBlocked.getTrustLevel()).isEqualTo(TrustLevel.NON_COMPLIANT);
    }
}
