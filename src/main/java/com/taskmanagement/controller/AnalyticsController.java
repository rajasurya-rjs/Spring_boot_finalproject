package com.taskmanagement.controller;

import com.taskmanagement.dto.ApiResponse;
import com.taskmanagement.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Analytics", description = "Analytics and statistics APIs using MongoDB aggregation")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get project statistics", description = "Retrieve analytics for a specific project")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectStatistics(@PathVariable String projectId) {
        Map<String, Object> stats = analyticsService.getProjectStatistics(projectId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user statistics", description = "Retrieve analytics for a specific user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(@PathVariable String userId) {
        Map<String, Object> stats = analyticsService.getUserStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user statistics", description = "Retrieve analytics for the currently authenticated user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserStatistics() {
        com.taskmanagement.security.UserPrincipal currentUser = com.taskmanagement.util.SecurityUtils
                .getCurrentUserPrincipal();
        Map<String, Object> stats = analyticsService.getUserStatistics(currentUser.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/overall")
    @Operation(summary = "Get overall statistics", description = "Retrieve overall system analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverallStatistics() {
        Map<String, Object> stats = analyticsService.getOverallStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
