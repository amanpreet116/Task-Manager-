package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByOwner_IdOrderByCreatedAtDesc(UUID ownerId);
}
