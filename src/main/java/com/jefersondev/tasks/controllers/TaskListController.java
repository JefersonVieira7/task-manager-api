package com.jefersondev.tasks.controllers;

import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.dto.TaskListDto;
import com.jefersondev.tasks.mappers.TaskListMapper;
import com.jefersondev.tasks.services.TaskListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-lists")
public class TaskListController {

    private final TaskListService taskListService;
    private final TaskListMapper taskListMapper;

    public TaskListController(TaskListService taskListService, TaskListMapper taskListMapper) {
        this.taskListService = taskListService;
        this.taskListMapper = taskListMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskListDto>> listTaskLists() {
        List<TaskListDto> taskLists = taskListService.listTaskLists()
                .stream()
                .map(taskListMapper::toDto)
                .toList();
        return ResponseEntity.ok(taskLists);
    }

    @PostMapping
    public ResponseEntity<TaskListDto> createTaskList(@Valid @RequestBody TaskListDto taskListDto) {
        TaskList created = taskListService.createTaskList(
                taskListMapper.fromDto(taskListDto)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskListMapper.toDto(created));
    }

    @GetMapping("/{task_list_id}")
    public ResponseEntity<TaskListDto> getTaskList(@PathVariable("task_list_id") UUID taskListId) {
        return taskListService.getTaskList(taskListId)
                .map(taskListMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{task_list_id}")
    public ResponseEntity<TaskListDto> updateTaskList(
            @PathVariable("task_list_id") UUID taskListId,
            @Valid @RequestBody TaskListDto taskListDto
    ) {
        TaskList updated = taskListService.updateTaskList(
                taskListId,
                taskListMapper.fromDto(taskListDto)
        );
        return ResponseEntity.ok(taskListMapper.toDto(updated));
    }

    @DeleteMapping("/{task_list_id}")
    public ResponseEntity<Void> deleteTaskList(@PathVariable("task_list_id") UUID taskListId) {
        taskListService.deleteTaskList(taskListId);
        return ResponseEntity.noContent().build();
    }
}
