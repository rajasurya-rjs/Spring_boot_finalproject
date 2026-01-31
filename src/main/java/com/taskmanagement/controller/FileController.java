package com.taskmanagement.controller;

import com.taskmanagement.dto.ApiResponse;
import com.taskmanagement.model.FileDocument;
import com.taskmanagement.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Files", description = "File upload and management APIs")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload file", description = "Upload a file and attach it to a task")
    public ResponseEntity<ApiResponse<FileDocument>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") String taskId) {
        FileDocument fileDocument = fileStorageService.uploadFile(file, taskId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", fileDocument));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file by ID", description = "Retrieve file metadata by ID")
    public ResponseEntity<ApiResponse<FileDocument>> getFileById(@PathVariable String id) {
        FileDocument fileDocument = fileStorageService.getFileById(id);
        return ResponseEntity.ok(ApiResponse.success(fileDocument));
    }

    @GetMapping
    @Operation(summary = "Get all files", description = "Retrieve all files with pagination")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<FileDocument>>> getAllFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<FileDocument> files = fileStorageService.getAllFiles(pageable);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get files by task", description = "Retrieve all files attached to a task")
    public ResponseEntity<ApiResponse<List<FileDocument>>> getFilesByTask(@PathVariable String taskId) {
        List<FileDocument> files = fileStorageService.getFilesByTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file", description = "Delete a file")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String id) {
        fileStorageService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
