package com.sinch.interview.trust;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProjectControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

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
}
