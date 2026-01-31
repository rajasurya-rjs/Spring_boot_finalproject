package com.taskmanagement.service;

import com.taskmanagement.exception.BadRequestException;
import com.taskmanagement.model.FileDocument;
import com.taskmanagement.model.Task;
import com.taskmanagement.repository.FileDocumentRepository;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.security.UserPrincipal;
import com.taskmanagement.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final FileDocumentRepository fileDocumentRepository;
    private final TaskRepository taskRepository;

    @Value("${application.file.upload-dir}")
    private String uploadDir;

    public FileStorageService(FileDocumentRepository fileDocumentRepository, TaskRepository taskRepository) {
        this.fileDocumentRepository = fileDocumentRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public FileDocument uploadFile(MultipartFile file, String taskId) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BadRequestException("Task not found"));

        UserPrincipal currentUser = SecurityUtils.getCurrentUserPrincipal();

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create file document
            FileDocument fileDocument = new FileDocument();
            fileDocument.setFileName(originalFilename);
            fileDocument.setContentType(file.getContentType());
            fileDocument.setSize(file.getSize());
            fileDocument.setFilePath(filePath.toString());
            fileDocument.setUploadedBy(currentUser.getUser());
            fileDocument.setTask(task);
            fileDocument.setUploadedAt(LocalDateTime.now());

            fileDocument = fileDocumentRepository.save(fileDocument);

            // Update task's attachment list
            task.getAttachments().add(fileDocument.getId());
            taskRepository.save(task);

            log.info("File uploaded: {} for task: {}", originalFilename, taskId);

            return fileDocument;

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    public FileDocument getFileById(String id) {
        return fileDocumentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("File not found"));
    }

    public List<FileDocument> getFilesByTask(String taskId) {
        return fileDocumentRepository.findByTaskId(taskId);
    }

    @Transactional
    public void deleteFile(String id) {
        FileDocument fileDocument = fileDocumentRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("File not found"));

        try {
            // Delete file from disk
            Path filePath = Paths.get(fileDocument.getFilePath());
            Files.deleteIfExists(filePath);

            // Remove from task's attachment list
            Task task = fileDocument.getTask();
            if (task != null) {
                task.getAttachments().remove(id);
                taskRepository.save(task);
            }

            // Delete file document
            fileDocumentRepository.delete(fileDocument);

            log.info("File deleted: {}", fileDocument.getFileName());

        } catch (IOException e) {
            log.error("Failed to delete file from disk", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }
}
