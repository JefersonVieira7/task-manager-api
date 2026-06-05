package com.jefersondev.tasks.controllers;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.dto.TaskListDto;
import com.jefersondev.tasks.mappers.TaskListMapper;
import com.jefersondev.tasks.services.TaskListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Tag(name = "Task Lists", description = "Endpoints para gerenciamento de listas de tarefas")
@RestController
@RequestMapping("/api/task-lists")
public class TaskListController {

    private final TaskListService taskListService;
    private final TaskListMapper taskListMapper;

    public TaskListController(TaskListService taskListService, TaskListMapper taskListMapper) {
        this.taskListService = taskListService;
        this.taskListMapper = taskListMapper;
    }

    @Operation(
            summary = "Listar todas as task lists",
            responses = @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    )
    @GetMapping
    public ResponseEntity<List<TaskListDto>> listTaskLists() {
        List<TaskListDto> taskLists = taskListService.listTaskLists()
                .stream()
                .map(taskListMapper::toDto)
                .toList();
        return ResponseEntity.ok(taskLists);
    }

    @Operation(
            summary = "Criar uma nova task list",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Task list criada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos")
            }
    )
    @PostMapping
    public ResponseEntity<TaskListDto> createTaskList(@Valid @RequestBody TaskListDto taskListDto) {
        TaskList created = taskListService.createTaskList(
                taskListMapper.fromDto(taskListDto)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskListMapper.toDto(created));
    }

    @Operation(
            summary = "Buscar task list por ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Task list encontrada"),
                    @ApiResponse(responseCode = "404", description = "Task list não encontrada")
            }
    )
    @GetMapping("/{task_list_id}")
    public ResponseEntity<TaskListDto> getTaskList(@PathVariable("task_list_id") UUID taskListId) {
        return taskListService.getTaskList(taskListId)
                .map(taskListMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Atualizar task list",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Atualizada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Task list não encontrada")
            }
    )
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

    @Operation(
            summary = "Deletar task list",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deletada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Task list não encontrada")
            }
    )
    @DeleteMapping("/{task_list_id}")
    public ResponseEntity<Void> deleteTaskList(@PathVariable("task_list_id") UUID taskListId) {
        taskListService.deleteTaskList(taskListId);
        return ResponseEntity.noContent().build();
    }
}