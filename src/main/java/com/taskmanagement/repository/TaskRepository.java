package com.taskmanagement.repository;

import com.taskmanagement.model.Priority;
import com.taskmanagement.model.Project;
import com.taskmanagement.model.Task;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    Page<Task> findByProject(Project project, Pageable pageable);

    Page<Task> findByAssignee(User assignee, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);

    Page<Task> findByAssigneeAndStatus(User assignee, TaskStatus status, Pageable pageable);

    List<Task> findByProjectAndStatusIn(Project project, List<TaskStatus> statuses);

    @Query("{ 'project.$id': ?0, 'status': ?1 }")
    long countByProjectIdAndStatus(String projectId, TaskStatus status);

    @Query("{ 'assignee.$id': ?0, 'status': { $in: ?1 } }")
    List<Task> findByAssigneeIdAndStatusIn(String assigneeId, List<TaskStatus> statuses);

    @Query("{ 'dueDate': { $gte: ?0, $lte: ?1 } }")
    List<Task> findTasksDueInRange(LocalDateTime start, LocalDateTime end);

    @Query("{ 'priority': ?0, 'status': { $ne: 'COMPLETED' } }")
    List<Task> findByPriorityAndNotCompleted(Priority priority);

    long countByProject(Project project);

    long countByProjectAndStatus(Project project, TaskStatus status);
}
