package com.sinch.interview.trust;

import com.sinch.interview.trust.domain.OutboxTarget;
import com.sinch.interview.trust.domain.TrustLevel;
import com.sinch.interview.trust.repository.BlockAuditRepository;
import com.sinch.interview.trust.repository.OutboxEventRepository;
import com.sinch.interview.trust.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ProjectControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    BlockAuditRepository blockAuditRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Test
    void returnsSeededProject() throws Exception {
        mockMvc.perform(get("/v1/projects/proj-cloud-limited"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("proj-cloud-limited"))
                .andExpect(jsonPath("$.accountType").value("CLOUD"))
                .andExpect(jsonPath("$.trustLevel").value("LIMITED"))
                .andExpect(jsonPath("$.blocked").value(false));
    }

    @Test
    void returns404WhenProjectMissing() throws Exception {
        mockMvc.perform(get("/v1/projects/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Project Not Found"));
    }

    @Test
    void blocksCloudProject() throws Exception {
        mockMvc.perform(post("/v1/projects/proj-cloud-trusted/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Suspected fraud","actor":"ops@sinch.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("proj-cloud-trusted"))
                .andExpect(jsonPath("$.blocked").value(true))
                .andExpect(jsonPath("$.trustLevel").value("NON_COMPLIANT"));

        var project = projectRepository.findById("proj-cloud-trusted").orElseThrow();
        assertThat(project.isBlocked()).isTrue();
        assertThat(project.getTrustLevel()).isEqualTo(TrustLevel.NON_COMPLIANT);

        var audits = blockAuditRepository.findByProjectIdOrderByCreatedAtDesc("proj-cloud-trusted");
        assertThat(audits).hasSize(1);
        assertThat(audits.getFirst().getActor()).isEqualTo("ops@sinch.com");
        assertThat(audits.getFirst().getReason()).isEqualTo("Suspected fraud");
        assertThat(audits.getFirst().getPreviousTrustLevel()).isEqualTo(TrustLevel.TRUSTED);
        assertThat(audits.getFirst().getNewTrustLevel()).isEqualTo(TrustLevel.NON_COMPLIANT);

        var outbox = outboxEventRepository.findByAggregateIdOrderByCreatedAtAsc("proj-cloud-trusted");
        assertThat(outbox).hasSize(2);
        assertThat(outbox).extracting(event -> event.getTarget())
                .containsExactlyInAnyOrder(OutboxTarget.GATEWAY_CACHE, OutboxTarget.COMPLIANCE_REPORT);
        assertThat(outbox).allMatch(event -> "ProjectBlocked".equals(event.getEventType()));
    }

    @Test
    void returns409WhenBlockingExemptProject() throws Exception {
        mockMvc.perform(post("/v1/projects/proj-enterprise-exempt/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Should fail","actor":"ops@sinch.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Failed Precondition"));
    }

    @Test
    void returns409WhenAlreadyBlocked() throws Exception {
        mockMvc.perform(post("/v1/projects/proj-already-blocked/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Should fail","actor":"ops@sinch.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Failed Precondition"));
    }

    @Test
    void returns404WhenBlockingMissingProject() throws Exception {
        mockMvc.perform(post("/v1/projects/does-not-exist/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"n/a","actor":"ops@sinch.com"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Project Not Found"));
    }

    @Test
    void returns400WhenReasonOrActorBlank() throws Exception {
        mockMvc.perform(post("/v1/projects/proj-cloud-limited/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"","actor":"ops@sinch.com"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/v1/projects/proj-cloud-limited/block")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"fraud","actor":""}
                                """))
                .andExpect(status().isBadRequest());
    }
}
