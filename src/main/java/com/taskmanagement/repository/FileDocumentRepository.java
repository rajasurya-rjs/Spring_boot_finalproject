package com.taskmanagement.repository;

import com.taskmanagement.model.FileDocument;
import com.taskmanagement.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDocumentRepository extends MongoRepository<FileDocument, String> {

    List<FileDocument> findByTask(Task task);

    List<FileDocument> findByTaskId(String taskId);
}
