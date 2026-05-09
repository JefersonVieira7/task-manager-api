package com.jefersondev.tasks.controllers;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.dto.TaskListDto;
import com.jefersondev.tasks.exceptions.ResourceNotFoundException;
import com.jefersondev.tasks.mappers.TaskListMapper;
import com.jefersondev.tasks.services.TaskListService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskListController — Unit Tests")
class TaskListControllerTest {

    @Mock
    private TaskListService taskListService;

    @Mock
    private TaskListMapper taskListMapper;

    @InjectMocks
    private TaskListController taskListController;

    @Test
    @DisplayName("GET /api/task-lists — should return 200 with list")
    void listTaskLists_shouldReturn200() {
        UUID id = UUID.randomUUID();
        TaskList entity = buildTaskList(id, "Work");
        TaskListDto dto = buildTaskListDto(id, "Work");

        when(taskListService.listTaskLists()).thenReturn(List.of(entity));
        when(taskListMapper.toDto(entity)).thenReturn(dto);

        ResponseEntity<List<TaskListDto>> response = taskListController.listTaskLists();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).title()).isEqualTo("Work");
    }

    @Test
    @DisplayName("POST /api/task-lists — should return 201 when valid")
    void createTaskList_shouldReturn201() {
        UUID id = UUID.randomUUID();
        TaskListDto requestDto = buildTaskListDto(null, "New List");
        TaskList entity = buildTaskList(null, "New List");
        TaskList saved = buildTaskList(id, "New List");
        TaskListDto responseDto = buildTaskListDto(id, "New List");

        when(taskListMapper.fromDto(any())).thenReturn(entity);
        when(taskListService.createTaskList(any())).thenReturn(saved);
        when(taskListMapper.toDto(saved)).thenReturn(responseDto);

        ResponseEntity<TaskListDto> response = taskListController.createTaskList(requestDto);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("New List");
        assertThat(response.getBody().id()).isEqualTo(id);
    }

    @Test
    @DisplayName("GET /api/task-lists/{id} — should return 200 when found")
    void getTaskList_shouldReturn200WhenFound() {
        UUID id = UUID.randomUUID();
        TaskList entity = buildTaskList(id, "Found");
        TaskListDto dto = buildTaskListDto(id, "Found");

        when(taskListService.getTaskList(id)).thenReturn(Optional.of(entity));
        when(taskListMapper.toDto(entity)).thenReturn(dto);

        ResponseEntity<TaskListDto> response = taskListController.getTaskList(id);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Found");
    }

    @Test
    @DisplayName("GET /api/task-lists/{id} — should return 404 when not found")
    void getTaskList_shouldReturn404WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(taskListService.getTaskList(id)).thenReturn(Optional.empty());

        ResponseEntity<TaskListDto> response = taskListController.getTaskList(id);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("DELETE /api/task-lists/{id} — should return 204 when deleted")
    void deleteTaskList_shouldReturn204() {
        UUID id = UUID.randomUUID();
        doNothing().when(taskListService).deleteTaskList(id);

        ResponseEntity<Void> response = taskListController.deleteTaskList(id);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
        verify(taskListService).deleteTaskList(id);
    }

    @Test
    @DisplayName("DELETE /api/task-lists/{id} — should throw when not found")
    void deleteTaskList_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Task list", id))
                .when(taskListService).deleteTaskList(id);

        assertThatThrownBy(() -> taskListController.deleteTaskList(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task list");
    }

    private TaskList buildTaskList(UUID id, String title) {
        TaskList tl = new TaskList();
        tl.setId(id);
        tl.setTitle(title);
        return tl;
    }

    private TaskListDto buildTaskListDto(UUID id, String title) {
        return new TaskListDto(id, title, null, 0, null, null);
    }
}