package com.jefersondev.tasks.controllers;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import com.jefersondev.tasks.domain.entities.dto.TaskDto;
import com.jefersondev.tasks.mappers.TaskMapper;
import com.jefersondev.tasks.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Tasks", description = "Gerenciamento de tarefas dentro de uma task list")
@RestController
@RequestMapping("/api/task-lists/{task_list_id}/tasks")
public class TasksController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TasksController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Operation(summary = "Listar tasks com paginação e filtros opcionais")
    @GetMapping
    public ResponseEntity<Page<TaskDto>> listTasks(
            @PathVariable("task_list_id") UUID taskListId,

            @Parameter(description = "Filtrar por status: OPEN, IN_PROGRESS, COMPLETED")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Buscar por título (parcial, case-insensitive)")
            @RequestParam(required = false) String title,

            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable
    ) {
        Page<TaskDto> tasks = taskService
                .listTasks(taskListId, status, title, pageable)
                .map(taskMapper::toDto);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Criar uma nova task")
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @PathVariable("task_list_id") UUID taskListId,
            @Valid @RequestBody TaskDto taskDto
    ) {
        Task created = taskService.createTask(taskListId, taskMapper.fromDto(taskDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toDto(created));
    }

    @Operation(summary = "Buscar task por ID")
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

    @Operation(summary = "Atualizar uma task")
    @PutMapping("/{task_id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable("task_list_id") UUID taskListId,
            @PathVariable("task_id") UUID taskId,
            @Valid @RequestBody TaskDto taskDto
    ) {
        Task updated = taskService.updateTask(taskListId, taskId, taskMapper.fromDto(taskDto));
        return ResponseEntity.ok(taskMapper.toDto(updated));
    }

    @Operation(summary = "Deletar uma task")
    @DeleteMapping("/{task_id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("task_list_id") UUID taskListId,
            @PathVariable("task_id") UUID taskId
    ) {
        taskService.deleteTask(taskListId, taskId);
        return ResponseEntity.noContent().build();
    }
}