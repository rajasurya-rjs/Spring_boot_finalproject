package com.taskmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private UserSummary owner;
    private List<UserSummary> members;
    private boolean archived;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Long> taskStatistics;

    public ProjectResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserSummary getOwner() {
        return owner;
    }

    public void setOwner(UserSummary owner) {
        this.owner = owner;
    }

    public List<UserSummary> getMembers() {
        return members;
    }

    public void setMembers(List<UserSummary> members) {
        this.members = members;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Long> getTaskStatistics() {
        return taskStatistics;
    }

    public void setTaskStatistics(Map<String, Long> taskStatistics) {
        this.taskStatistics = taskStatistics;
    }
}
