package com.jefersondev.tasks.services.impl;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.services.TaskListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskListServiceImpl implements TaskListService {

    private final TaskListRepository taskListRepository;

    public TaskListServiceImpl(TaskListRepository taskListRepository) {
        this.taskListRepository = taskListRepository;
    }

    @Override
    public List<TaskList> listTaskLists() {
        return taskListRepository.findAllWithTasks();
    }

    @Transactional
    @Override
    public TaskList createTaskList(TaskList taskList) {
        if (null != taskList.getId()) {
            throw new IllegalArgumentException("Task list already has an ID!");
        }
        if (null == taskList.getTitle() || taskList.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task list title must be present!");
        }

        return taskListRepository.save(new TaskList(
                null,
                taskList.getTitle(),
                taskList.getDescription(),
                null,
                null,
                null
        ));
    }

    @Override
    public Optional<TaskList> getTaskList(UUID id) {
        return taskListRepository.findByIdWithTasks(id);
    }

    @Transactional
    @Override
    public TaskList updateTaskList(UUID taskListId, TaskList taskList) {
        if (taskList.getId() == null) {
            throw new IllegalArgumentException("Task list must have an ID");
        }
        if (!Objects.equals(taskList.getId(), taskListId)) {
            throw new IllegalArgumentException("Attempting to change task list ID, this is not permitted!");
        }

        TaskList existing = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new IllegalArgumentException("Task list not found!"));

        existing.setTitle(taskList.getTitle());
        existing.setDescription(taskList.getDescription());
        return taskListRepository.save(existing);
    }

    @Transactional
    @Override
    public void deleteTaskList(UUID taskListId) {
        taskListRepository.deleteById(taskListId);
    }
}