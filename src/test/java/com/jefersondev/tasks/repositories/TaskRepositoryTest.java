package com.jefersondev.tasks.repositories;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.TaskPriority;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("TaskRepository — Repository Tests")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    private TaskList taskList;

    @BeforeEach
    void setUp() {
        TaskList tl = new TaskList();
        tl.setTitle("Test List");
        taskList = taskListRepository.save(tl);
    }


    @Test
    @DisplayName("findByTaskListIdAndId — should return task when found")
    void findByTaskListIdAndId_shouldReturnWhenFound() {
        Task saved = taskRepository.save(
                buildTask("My Task", TaskStatus.OPEN, TaskPriority.HIGH));

        Optional<Task> result = taskRepository
                .findByTaskListIdAndId(taskList.getId(), saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("My Task");
    }

    @Test
    @DisplayName("findByTaskListIdAndId — should return empty when task belongs to different list")
    void findByTaskListIdAndId_shouldReturnEmptyWhenDifferentList() {
        Task saved = taskRepository.save(
                buildTask("My Task", TaskStatus.OPEN, TaskPriority.HIGH));

        Optional<Task> result = taskRepository
                .findByTaskListIdAndId(UUID.randomUUID(), saved.getId());

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("findByTaskListIdWithFilters — no filters should return all tasks")
    void findWithFilters_noFilters_shouldReturnAll() {
        taskRepository.save(buildTask("Task A", TaskStatus.OPEN, TaskPriority.HIGH));
        taskRepository.save(buildTask("Task B", TaskStatus.COMPLETED, TaskPriority.LOW));
        taskRepository.save(buildTask("Task C", TaskStatus.OPEN, TaskPriority.MEDIUM));

        Page<Task> result = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("findByTaskListIdWithFilters — filter by status should return only matching")
    void findWithFilters_filterByStatus_shouldReturnOnlyMatching() {
        taskRepository.save(buildTask("Open Task 1", TaskStatus.OPEN, TaskPriority.HIGH));
        taskRepository.save(buildTask("Open Task 2", TaskStatus.OPEN, TaskPriority.LOW));
        taskRepository.save(buildTask("Done Task", TaskStatus.COMPLETED, TaskPriority.MEDIUM));

        Page<Task> result = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), TaskStatus.OPEN, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Task::getStatus)
                .containsOnly(TaskStatus.OPEN);
    }

    @Test
    @DisplayName("findByTaskListIdWithFilters — filter by title should return matching tasks")
    void findWithFilters_filterByTitle_shouldReturnMatching() {
        taskRepository.save(buildTask("Study Spring Boot", TaskStatus.OPEN, TaskPriority.HIGH));
        taskRepository.save(buildTask("Study Docker", TaskStatus.OPEN, TaskPriority.MEDIUM));
        taskRepository.save(buildTask("Buy groceries", TaskStatus.OPEN, TaskPriority.LOW));

        Page<Task> result = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), null, "study", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Study Spring Boot", "Study Docker");
    }

    @Test
    @DisplayName("findByTaskListIdWithFilters — title filter is case-insensitive")
    void findWithFilters_titleFilterIsCaseInsensitive() {
        taskRepository.save(buildTask("Study Spring", TaskStatus.OPEN, TaskPriority.HIGH));

        Page<Task> upper = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), null, "STUDY", PageRequest.of(0, 10));
        Page<Task> lower = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), null, "study", PageRequest.of(0, 10));

        assertThat(upper.getTotalElements()).isEqualTo(1);
        assertThat(lower.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findByTaskListIdWithFilters — combining status and title filters")
    void findWithFilters_combiningFilters_shouldReturnOnlyMatching() {
        taskRepository.save(buildTask("Study Java", TaskStatus.OPEN, TaskPriority.HIGH));
        taskRepository.save(buildTask("Study Docker", TaskStatus.COMPLETED, TaskPriority.LOW));
        taskRepository.save(buildTask("Buy coffee", TaskStatus.OPEN, TaskPriority.MEDIUM));

        Page<Task> result = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), TaskStatus.OPEN, "study", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Study Java");
    }

    @Test
    @DisplayName("findByTaskListIdWithFilters — pagination should limit results correctly")
    void findWithFilters_pagination_shouldLimitResults() {
        for (int i = 1; i <= 7; i++) {
            taskRepository.save(buildTask("Task " + i, TaskStatus.OPEN, TaskPriority.LOW));
        }

        Page<Task> page0 = taskRepository.findByTaskListIdWithFilters(
                taskList.getId(), null, null, PageRequest.of(0, 3));

        assertThat(page0.getContent()).hasSize(3);
        assertThat(page0.getTotalElements()).isEqualTo(7);
        assertThat(page0.getTotalPages()).isEqualTo(3);
    }

    private Task buildTask(String title, TaskStatus status, TaskPriority priority) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setTaskList(taskList);
        return task;
    }
}