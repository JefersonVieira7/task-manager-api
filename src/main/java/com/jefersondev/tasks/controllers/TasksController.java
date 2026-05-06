package com.jefersondev.tasks.controllers;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.dto.TaskDto;
import com.jefersondev.tasks.mappers.TaskMapper;
import com.jefersondev.tasks.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/task-lists/{task_list_id}/tasks")
public class TasksController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TasksController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> listTasks(
            @PathVariable("task_list_id") UUID taskListId
    ) {
        List<TaskDto> tasks = taskService.listTasks(taskListId)
                .stream()
                .map(taskMapper::toDto)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @PathVariable("task_list_id") UUID taskListId,
            @Valid @RequestBody TaskDto taskDto
    ) {
        Task created = taskService.createTask(
                taskListId,
                taskMapper.fromDto(taskDto)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toDto(created));
    }

    @GetMapping("/{task_id}")
    public ResponseEntity<TaskDto> getTask(
            @PathVariable("task_list_id") UUID taskListId,
            @PathVariable("task_id") UUID taskId
    ) {
        return taskService.getTask(taskListId, taskId)
                .map(taskMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{task_id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable("task_list_id") UUID taskListId,
            @PathVariable("task_id") UUID taskId,
            @Valid @RequestBody TaskDto taskDto
    ) {
        Task updated = taskService.updateTask(
                taskListId,
                taskId,
                taskMapper.fromDto(taskDto)
        );
        return ResponseEntity.ok(taskMapper.toDto(updated));
    }

    @DeleteMapping("/{task_id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("task_list_id") UUID taskListId,
            @PathVariable("task_id") UUID taskId
    ) {
        taskService.deleteTask(taskListId, taskId);
        return ResponseEntity.noContent().build();
    }
}
