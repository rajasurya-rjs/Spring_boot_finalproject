package com.taskmanagement.repository;

import com.taskmanagement.model.Project;
import com.taskmanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    Page<Project> findByOwner(User owner, Pageable pageable);

    Page<Project> findByMembersContaining(User user, Pageable pageable);

    @Query("{ $or: [ { 'owner.$id': ?0 }, { 'members.$id': ?0 } ] }")
    Page<Project> findByOwnerOrMember(String userId, Pageable pageable);

    List<Project> findByArchivedFalse();

    long countByOwner(User owner);
}
