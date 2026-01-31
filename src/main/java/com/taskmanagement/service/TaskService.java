package com.taskmanagement.service;

import com.taskmanagement.dto.TaskFilterRequest;
import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.dto.UserSummary;
import com.taskmanagement.exception.ResourceNotFoundException;
import com.taskmanagement.model.Project;
import com.taskmanagement.model.Task;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.ProjectRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.UserPrincipal;
import com.taskmanagement.util.SecurityUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TaskService(UserRepository userRepository, ProjectRepository projectRepository,
            TaskRepository taskRepository, EmailService emailService, MongoTemplate mongoTemplate,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.emailService = emailService;
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse createTask(TaskRequest request) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setCreator(currentUser.getUser());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setTags(request.getTags());
        task.setEstimatedHours(request.getEstimatedHours());

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            task.setAssignee(assignee);

            // Send email notification
            emailService.sendTaskAssignmentEmail(
                    assignee.getEmail(),
                    assignee.getFullName(),
                    task.getTitle(),
                    project.getName());

            // Publish Kafka event
            publishTaskEvent("TASK_ASSIGNED", task.getId(), assignee.getId());
        }

        task = taskRepository.save(task);
        log.info("Task created: {} in project: {}", task.getTitle(), project.getName());

        // Publish Kafka event
        publishTaskEvent("TASK_CREATED", task.getId(), currentUser.getUser().getId());

        return mapToResponse(task);
    }

    @Cacheable(value = "tasks", key = "#id")
    public TaskResponse getTaskById(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        return mapToResponse(task);
    }

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        Page<Task> tasks = taskRepository.findAll(pageable);
        return tasks.map(this::mapToResponse);
    }

    public Page<TaskResponse> filterTasks(TaskFilterRequest filter) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        if (filter.getProjectId() != null) {
            criteria.add(Criteria.where("project.$id").is(filter.getProjectId()));
        }

        if (filter.getAssigneeId() != null) {
            criteria.add(Criteria.where("assignee.$id").is(filter.getAssigneeId()));
        }

        if (filter.getStatus() != null) {
            criteria.add(Criteria.where("status").is(filter.getStatus()));
        }

        if (filter.getPriority() != null) {
            criteria.add(Criteria.where("priority").is(filter.getPriority()));
        }

        if (filter.getDueDateFrom() != null && filter.getDueDateTo() != null) {
            criteria.add(Criteria.where("dueDate").gte(filter.getDueDateFrom()).lte(filter.getDueDateTo()));
        }

        if (filter.getCreatedFrom() != null && filter.getCreatedTo() != null) {
            criteria.add(Criteria.where("createdAt").gte(filter.getCreatedFrom()).lte(filter.getCreatedTo()));
        }

        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(filter.getSearchTerm(), "i"),
                    Criteria.where("description").regex(filter.getSearchTerm(), "i"));
            criteria.add(searchCriteria);
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        Sort.Direction direction = filter.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(direction, filter.getSortBy()));

        query.with(pageable);

        List<Task> tasks = mongoTemplate.find(query, Task.class);
        long count = mongoTemplate.count(query.skip(0).limit(0), Task.class);

        Page<Task> taskPage = PageableExecutionUtils.getPage(
                tasks,
                pageable,
                () -> count);

        return taskPage.map(this::mapToResponse);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public TaskResponse updateTask(String id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setTags(request.getTags());
        task.setEstimatedHours(request.getEstimatedHours());
        task.setUpdatedAt(LocalDateTime.now());

        if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(request.getStatus());

            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }

            publishTaskEvent("TASK_STATUS_CHANGED", task.getId(),
                    String.format("%s->%s", oldStatus, request.getStatus()));
        }

        if (request.getAssigneeId() != null &&
                (task.getAssignee() == null || !task.getAssignee().getId().equals(request.getAssigneeId()))) {
            User newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            task.setAssignee(newAssignee);

            emailService.sendTaskAssignmentEmail(
                    newAssignee.getEmail(),
                    newAssignee.getFullName(),
                    task.getTitle(),
                    task.getProject().getName());

            publishTaskEvent("TASK_REASSIGNED", task.getId(), newAssignee.getId());
        }

        task = taskRepository.save(task);
        log.info("Task updated: {}", task.getTitle());

        return mapToResponse(task);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        taskRepository.delete(task);
        log.info("Task deleted: {}", task.getTitle());

        publishTaskEvent("TASK_DELETED", id, null);
    }

    private void publishTaskEvent(String eventType, String taskId, String metadata) {
        try {
            String event = String.format("{\"type\":\"%s\",\"taskId\":\"%s\",\"metadata\":\"%s\",\"timestamp\":\"%s\"}",
                    eventType, taskId, metadata, LocalDateTime.now());
            kafkaTemplate.send("task-events", taskId, event);
            log.info("Published Kafka event: {} for task: {}", eventType, taskId);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event", e);
        }
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setProjectId(task.getProject().getId());
        response.setProjectName(task.getProject().getName());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setTags(task.getTags());
        response.setEstimatedHours(task.getEstimatedHours());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        // Map creator info
        if (task.getCreator() != null) {
            UserSummary creator = new UserSummary();
            creator.setId(task.getCreator().getId());
            creator.setUsername(task.getCreator().getUsername());
            creator.setFullName(task.getCreator().getFullName());
            response.setCreator(creator);
        }

        // Map assignee if present
        if (task.getAssignee() != null) {
            UserSummary assignee = new UserSummary();
            assignee.setId(task.getAssignee().getId());
            assignee.setUsername(task.getAssignee().getUsername());
            assignee.setFullName(task.getAssignee().getFullName());
            response.setAssignee(assignee);
        }

        return response;
    }
}
