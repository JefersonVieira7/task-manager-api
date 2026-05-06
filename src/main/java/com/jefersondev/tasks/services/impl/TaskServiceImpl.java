// src/main/java/com/jefersondev/tasks/services/impl/TaskServiceImpl.java
package com.jefersondev.tasks.services.impl;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.TaskPriority;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import com.jefersondev.tasks.exceptions.BusinessException;
import com.jefersondev.tasks.exceptions.ResourceNotFoundException;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.repositories.TaskRepository;
import com.jefersondev.tasks.services.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskListRepository taskListRepository) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
    }

    @Override
    public List<Task> listTasks(UUID taskListId) {
        return taskRepository.findByTaskListId(taskListId);
    }

    @Transactional
    @Override
    public Task createTask(UUID taskListId, Task task) {
        if (task.getId() != null) {
            throw new BusinessException("Task must not have an ID when creating");
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new BusinessException("Task must have a title");
        }

        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list", taskListId));

        Task taskToSave = new Task(
                null,
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                TaskStatus.OPEN,
                Optional.ofNullable(task.getPriority()).orElse(TaskPriority.MEDIUM),
                taskList,
                null,
                null
        );

        return taskRepository.save(taskToSave);
    }

    @Override
    public Optional<Task> getTask(UUID taskListId, UUID taskId) {
        return taskRepository.findByTaskListIdAndId(taskListId, taskId);
    }

    @Transactional
    @Override
    public Task updateTask(UUID taskListId, UUID taskId, Task task) {
        if (task.getId() == null) {
            throw new BusinessException("Task must have an ID to be updated");
        }
        if (!taskId.equals(task.getId())) {
            throw new BusinessException("Task ID in the body does not match the URL");
        }
        if (task.getPriority() == null) {
            throw new BusinessException("Task must have a valid priority");
        }
        if (task.getStatus() == null) {
            throw new BusinessException("Task must have a valid status");
        }

        Task existing = taskRepository.findByTaskListIdAndId(taskListId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setDueDate(task.getDueDate());
        existing.setPriority(task.getPriority());
        existing.setStatus(task.getStatus());
        return taskRepository.save(existing);
    }

    @Transactional
    @Override
    public void deleteTask(UUID taskListId, UUID taskId) {
        Task task = taskRepository.findByTaskListIdAndId(taskListId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        taskRepository.delete(task);
    }
}