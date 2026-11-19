package com.sinch.interview.trust.repository;

import com.sinch.interview.trust.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, String> {
}
