package com.taskmanagement.service;

import com.taskmanagement.dto.ProjectRequest;
import com.taskmanagement.dto.ProjectResponse;
import com.taskmanagement.dto.UserSummary;
import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.model.Project;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.UserPrincipal;
import com.taskmanagement.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
            TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse createProject(ProjectRequest request) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();
        User owner = currentUser.getUser();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
        }

        project = projectRepository.save(project);
        log.info("Project created: {} by user: {}", project.getName(), owner.getUsername());

        return mapToResponse(project);
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        return mapToResponse(project);
    }

    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();
        Page<Project> projects = projectRepository.findByOwnerOrMember(
                currentUser.getUser().getId(),
                pageable);

        return projects.map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public ProjectResponse updateProject(String id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();
        if (!project.getOwner().getId().equals(currentUser.getUser().getId())) {
            throw new BadRequestException("Only project owner can update the project");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedAt(LocalDateTime.now());

        if (request.getMemberIds() != null) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            project.setMembers(members);
        }

        project = projectRepository.save(project);
        log.info("Project updated: {}", project.getName());

        return mapToResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", allEntries = true)
    public void deleteProject(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();
        if (!project.getOwner().getId().equals(currentUser.getUser().getId())) {
            throw new BadRequestException("Only project owner can delete the project");
        }

        projectRepository.delete(project);
        log.info("Project deleted: {}", project.getName());
    }

    private ProjectResponse mapToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setStartDate(project.getStartDate());
        response.setEndDate(project.getEndDate());
        response.setArchived(project.isArchived());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());

        if (project.getMembers() != null) {
            response.setMembers(project.getMembers().stream()
                    .map(member -> new UserSummary(
                            member.getId(),
                            member.getUsername(),
                            member.getEmail(),
                            member.getFullName()))
                    .collect(Collectors.toList()));
        } else {
            response.setMembers(new ArrayList<>());
        }

        long totalTasks = taskRepository.countByProject(project);
        long completedTasks = taskRepository.countByProjectAndStatus(project, TaskStatus.COMPLETED);

        return response;
    }
}
