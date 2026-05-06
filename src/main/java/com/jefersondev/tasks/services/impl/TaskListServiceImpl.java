package com.jefersondev.tasks.services.impl;

import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.exceptions.BusinessException;
import com.jefersondev.tasks.exceptions.ResourceNotFoundException;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.services.TaskListService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (taskList.getId() != null) {
            throw new BusinessException("Task list must not have an ID when creating");
        }
        if (taskList.getTitle() == null || taskList.getTitle().isBlank()) {
            throw new BusinessException("Task list title must be present");
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
            throw new BusinessException("Task list must have an ID to be updated");
        }
        if (!Objects.equals(taskList.getId(), taskListId)) {
            throw new BusinessException("Task list ID in the body does not match the URL");
        }

        TaskList existing = taskListRepository.findById(taskListId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list", taskListId));

        existing.setTitle(taskList.getTitle());
        existing.setDescription(taskList.getDescription());
        return taskListRepository.save(existing);
    }

    @Transactional
    @Override
    public void deleteTaskList(UUID taskListId) {
        if (!taskListRepository.existsById(taskListId)) {
            throw new ResourceNotFoundException("Task list", taskListId);
        }
        taskListRepository.deleteById(taskListId);
    }
}