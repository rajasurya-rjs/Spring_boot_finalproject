package com.taskmanagement.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectRequest {

    private String name;

    private String description;

    private List<String> memberIds = new ArrayList<>();

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public ProjectRequest() {
    }

    public ProjectRequest(String name, String description, List<String> memberIds,
            LocalDateTime startDate, LocalDateTime endDate) {
        this.name = name;
        this.description = description;
        this.memberIds = memberIds != null ? memberIds : new ArrayList<>();
        this.startDate = startDate;
        this.endDate = endDate;
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

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
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
}
