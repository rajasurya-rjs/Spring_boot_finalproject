package com.taskmanagement.service;

import com.taskmanagement.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final TaskRepository taskRepository;
    private final MongoTemplate mongoTemplate;

    public AnalyticsService(TaskRepository taskRepository, MongoTemplate mongoTemplate) {
        this.taskRepository = taskRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> getProjectStatistics(String projectId) {
        Map<String, Object> stats = new HashMap<>();

        // Count tasks by status
        MatchOperation matchProject = match(
                org.springframework.data.mongodb.core.query.Criteria.where("project.$id").is(projectId));
        GroupOperation groupByStatus = group("status").count().as("count");

        Aggregation aggregation = newAggregation(matchProject, groupByStatus);
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "tasks", Map.class);

        Map<String, Long> statusCounts = new HashMap<>();
        results.getMappedResults().forEach(result -> {
            statusCounts.put(result.get("_id").toString(), ((Number) result.get("count")).longValue());
        });

        stats.put("tasksByStatus", statusCounts);

        // Count tasks by priority
        GroupOperation groupByPriority = group("priority").count().as("count");
        Aggregation priorityAggregation = newAggregation(matchProject, groupByPriority);
        AggregationResults<Map> priorityResults = mongoTemplate.aggregate(priorityAggregation, "tasks", Map.class);

        Map<String, Long> priorityCounts = new HashMap<>();
        priorityResults.getMappedResults().forEach(result -> {
            priorityCounts.put(result.get("_id").toString(), ((Number) result.get("count")).longValue());
        });

        stats.put("tasksByPriority", priorityCounts);

        return stats;
    }

    public Map<String, Object> getUserStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        // Count assigned tasks by status
        MatchOperation matchUser = match(
                org.springframework.data.mongodb.core.query.Criteria.where("assignee.$id").is(userId));
        GroupOperation groupByStatus = group("status").count().as("count");

        Aggregation aggregation = newAggregation(matchUser, groupByStatus);
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "tasks", Map.class);

        Map<String, Long> statusCounts = new HashMap<>();
        results.getMappedResults().forEach(result -> {
            statusCounts.put(result.get("_id").toString(), ((Number) result.get("count")).longValue());
        });

        stats.put("assignedTasksByStatus", statusCounts);

        return stats;
    }

    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Total tasks count
        long totalTasks = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "tasks");
        stats.put("totalTasks", totalTasks);

        // Total projects count
        long totalProjects = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "projects");
        stats.put("totalProjects", totalProjects);

        // Total users count
        long totalUsers = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), "users");
        stats.put("totalUsers", totalUsers);

        // Tasks by status (overall)
        GroupOperation groupByStatus = group("status").count().as("count");
        Aggregation statusAggregation = newAggregation(groupByStatus);
        AggregationResults<Map> statusResults = mongoTemplate.aggregate(statusAggregation, "tasks", Map.class);

        Map<String, Long> statusCounts = new HashMap<>();
        statusResults.getMappedResults().forEach(result -> {
            statusCounts.put(result.get("_id").toString(), ((Number) result.get("count")).longValue());
        });

        stats.put("tasksByStatus", statusCounts);

        // Tasks by priority (overall)
        GroupOperation groupByPriority = group("priority").count().as("count");
        Aggregation priorityAggregation = newAggregation(groupByPriority);
        AggregationResults<Map> priorityResults = mongoTemplate.aggregate(priorityAggregation, "tasks", Map.class);

        Map<String, Long> priorityCounts = new HashMap<>();
        priorityResults.getMappedResults().forEach(result -> {
            priorityCounts.put(result.get("_id").toString(), ((Number) result.get("count")).longValue());
        });

        stats.put("tasksByPriority", priorityCounts);

        return stats;
    }
}
