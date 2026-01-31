package com.taskmanagement.controller;

import com.taskmanagement.dto.ApiResponse;
import com.taskmanagement.service.ExternalApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/integrations")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Integrations", description = "External API integration endpoints")
public class IntegrationController {

    private final ExternalApiService externalApiService;

    public IntegrationController(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    @GetMapping("/currency/convert")
    @Operation(summary = "Convert currency", description = "Convert amount from one currency to another using external API")
    public ResponseEntity<ApiResponse<Map<String, Object>>> convertCurrency(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam Double amount) {
        Map<String, Object> result = externalApiService.convertCurrency(from, to, amount);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
