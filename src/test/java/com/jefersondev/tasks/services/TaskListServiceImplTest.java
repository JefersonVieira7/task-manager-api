package com.jefersondev.tasks.services;

import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.exceptions.BusinessException;
import com.jefersondev.tasks.exceptions.ResourceNotFoundException;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.services.impl.TaskListServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskListService — Unit Tests")
class TaskListServiceImplTest {

    @Mock
    private TaskListRepository taskListRepository;

    @InjectMocks
    private TaskListServiceImpl taskListService;

    // ─── listTaskLists ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listTaskLists — should return all task lists")
    void listTaskLists_shouldReturnAll() {
        TaskList list1 = buildTaskList(UUID.randomUUID(), "Work");
        TaskList list2 = buildTaskList(UUID.randomUUID(), "Personal");
        when(taskListRepository.findAllWithTasks()).thenReturn(List.of(list1, list2));

        List<TaskList> result = taskListService.listTaskLists();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TaskList::getTitle)
                .containsExactlyInAnyOrder("Work", "Personal");
        verify(taskListRepository, times(1)).findAllWithTasks();
    }

    @Test
    @DisplayName("listTaskLists — should return empty list when no task lists exist")
    void listTaskLists_shouldReturnEmpty() {
        when(taskListRepository.findAllWithTasks()).thenReturn(List.of());

        List<TaskList> result = taskListService.listTaskLists();

        assertThat(result).isEmpty();
    }

    // ─── createTaskList ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createTaskList — should create successfully with valid data")
    void createTaskList_shouldCreateSuccessfully() {
        TaskList input = buildTaskList(null, "My List");
        TaskList saved = buildTaskList(UUID.randomUUID(), "My List");
        when(taskListRepository.save(any(TaskList.class))).thenReturn(saved);

        TaskList result = taskListService.createTaskList(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("My List");
        verify(taskListRepository, times(1)).save(any(TaskList.class));
    }

    @Test
    @DisplayName("createTaskList — should throw BusinessException when ID is provided")
    void createTaskList_shouldThrowWhenIdPresent() {
        TaskList input = buildTaskList(UUID.randomUUID(), "My List");

        assertThatThrownBy(() -> taskListService.createTaskList(input))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must not have an ID");

        verify(taskListRepository, never()).save(any());
    }

    // ─── getTaskList ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTaskList — should return task list when found")
    void getTaskList_shouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        TaskList taskList = buildTaskList(id, "Found List");
        when(taskListRepository.findByIdWithTasks(id)).thenReturn(Optional.of(taskList));

        Optional<TaskList> result = taskListService.getTaskList(id);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Found List");
    }

    @Test
    @DisplayName("getTaskList — should return empty when not found")
    void getTaskList_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(taskListRepository.findByIdWithTasks(id)).thenReturn(Optional.empty());

        Optional<TaskList> result = taskListService.getTaskList(id);

        assertThat(result).isEmpty();
    }

    // ─── updateTaskList ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTaskList — should update successfully")
    void updateTaskList_shouldUpdateSuccessfully() {
        UUID id = UUID.randomUUID();
        TaskList input = buildTaskList(id, "Updated Title");
        TaskList existing = buildTaskList(id, "Old Title");
        TaskList saved = buildTaskList(id, "Updated Title");

        when(taskListRepository.findById(id)).thenReturn(Optional.of(existing));
        when(taskListRepository.save(any(TaskList.class))).thenReturn(saved);

        TaskList result = taskListService.updateTaskList(id, input);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    @DisplayName("updateTaskList — should throw ResourceNotFoundException when not found")
    void updateTaskList_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        TaskList input = buildTaskList(id, "Title");
        when(taskListRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskListService.updateTaskList(id, input))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task list");

        verify(taskListRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateTaskList — should throw BusinessException when IDs do not match")
    void updateTaskList_shouldThrowWhenIdsMismatch() {
        UUID urlId = UUID.randomUUID();
        UUID bodyId = UUID.randomUUID();
        TaskList input = buildTaskList(bodyId, "Title");

        assertThatThrownBy(() -> taskListService.updateTaskList(urlId, input))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("does not match");
    }

    // ─── deleteTaskList ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTaskList — should delete when exists")
    void deleteTaskList_shouldDeleteWhenExists() {
        UUID id = UUID.randomUUID();
        when(taskListRepository.existsById(id)).thenReturn(true);

        assertThatCode(() -> taskListService.deleteTaskList(id))
                .doesNotThrowAnyException();

        verify(taskListRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteTaskList — should throw ResourceNotFoundException when not found")
    void deleteTaskList_shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(taskListRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> taskListService.deleteTaskList(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskListRepository, never()).deleteById(any());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private TaskList buildTaskList(UUID id, String title) {
        TaskList taskList = new TaskList();
        taskList.setId(id);
        taskList.setTitle(title);
        taskList.setDescription("Description");
        return taskList;
    }
}