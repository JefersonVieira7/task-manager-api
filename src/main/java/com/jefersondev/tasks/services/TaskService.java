package com.jefersondev.tasks.services;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TaskService {
    Page<Task> listTasks(UUID taskListId, TaskStatus status, String title, Pageable pageable);
    Task createTask(UUID taskListId, Task task);
    Optional<Task> getTask(UUID taskListID, UUID taskId);
    Task updateTask(UUID taskListId, UUID taskId, Task task);
    void deleteTask(UUID taskListId, UUID taskId);
}
