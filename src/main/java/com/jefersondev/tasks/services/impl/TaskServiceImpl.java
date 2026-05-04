package com.jefersondev.tasks.services.impl;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.TaskPriority;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.repositories.TaskRepository;
import com.jefersondev.tasks.services.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
        if (null != task.getId()) {
            throw new IllegalArgumentException("Task already has an ID!");
        }
        if (null == task.getTitle() || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task must have a title");
        }

        TaskPriority taskPriority = Optional.ofNullable(task.getPriority())
                .orElse(TaskPriority.MEDIUM);

        TaskList taskList = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Task List ID provided!"));

        Task taskToSave = new Task(
                null,
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                TaskStatus.OPEN,
                taskPriority,
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
        if (null == task.getId()) {
            throw new IllegalArgumentException("Task must have an ID!");
        }
        if (!Objects.equals(taskId, task.getId())) {
            throw new IllegalArgumentException("Task IDs do not match");
        }
        if (null == task.getPriority()) {
            throw new IllegalArgumentException("Task must have a valid priority!");
        }
        if (null == task.getStatus()) {
            throw new IllegalArgumentException("Task must have a valid status");
        }

        Task existing = taskRepository.findByTaskListIdAndId(taskListId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found!"));

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
        taskRepository.deleteByTaskListIdAndId(taskListId, taskId);
    }
}
