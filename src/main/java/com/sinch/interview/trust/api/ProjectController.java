package com.sinch.interview.trust.api;

import com.sinch.interview.trust.domain.Project;
import com.sinch.interview.trust.repository.ProjectRepository;
import com.sinch.interview.trust.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    public ProjectController(ProjectRepository projectRepository, ProjectService projectService) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> listProjects() {
        return projectRepository.findAll(Sort.by("id"))
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        return ProjectResponse.from(project);
    }

    @PostMapping("/{projectId}/block")
    public ProjectResponse blockProject(
            @PathVariable String projectId,
            @Valid @RequestBody BlockProjectRequest request
    ) {
        return projectService.blockProject(projectId, request.reason(), request.actor());
    }
}
