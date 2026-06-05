package com.jefersondev.tasks.repositories;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.TaskPriority;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("TaskListRepository — Repository Tests")
class TaskListRepositoryTest {

    @Autowired
    private     EntityManager entityManager;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;


    @Test
    @DisplayName("findAllWithTasks — should return all task lists")
    void findAllWithTasks_shouldReturnAll() {
        taskListRepository.save(buildTaskList("Work"));
        taskListRepository.save(buildTaskList("Personal"));

        List<TaskList> result = taskListRepository.findAllWithTasks();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TaskList::getTitle)
                .containsExactlyInAnyOrder("Work", "Personal");
    }

    @Test
    @DisplayName("findAllWithTasks — should return empty when no task lists exist")
    void findAllWithTasks_shouldReturnEmpty() {
        List<TaskList> result = taskListRepository.findAllWithTasks();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllWithTasks — should return task list with tasks loaded")
    void findAllWithTasks_shouldReturnWithTasksLoaded() {
        TaskList list = taskListRepository.save(buildTaskList("Work"));
        taskRepository.save(buildTask("Task A", TaskStatus.OPEN, TaskPriority.HIGH, list));
        taskRepository.save(buildTask("Task B", TaskStatus.OPEN, TaskPriority.LOW, list));

        entityManager.flush();
        entityManager.clear();

        List<TaskList> result = taskListRepository.findAllWithTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTasks()).hasSize(2);
    }


    @Test
    @DisplayName("findByIdWithTasks — should return task list with tasks when found")
    void findByIdWithTasks_shouldReturnWithTasks() {
        TaskList list = taskListRepository.save(buildTaskList("Work"));
        taskRepository.save(buildTask("Task A", TaskStatus.OPEN, TaskPriority.HIGH, list));
        taskRepository.save(buildTask("Task B", TaskStatus.COMPLETED, TaskPriority.LOW, list));

        entityManager.flush();
        entityManager.clear();

        Optional<TaskList> result = taskListRepository.findByIdWithTasks(list.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Work");
        assertThat(result.get().getTasks()).hasSize(2);
    }

    @Test
    @DisplayName("findByIdWithTasks — should return empty when not found")
    void findByIdWithTasks_shouldReturnEmptyWhenNotFound() {
        Optional<TaskList> result = taskListRepository.findByIdWithTasks(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdWithTasks — should return task list with no tasks")
    void findByIdWithTasks_shouldReturnTaskListWithNoTasks() {
        TaskList list = taskListRepository.save(buildTaskList("Empty"));

        entityManager.flush();
        entityManager.clear();

        Optional<TaskList> result = taskListRepository.findByIdWithTasks(list.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).isEmpty();
    }


    private TaskList buildTaskList(String title) {
        TaskList tl = new TaskList();
        tl.setTitle(title);
        tl.setDescription("Description of " + title);
        return tl;
    }

    private Task buildTask(String title, TaskStatus status,
                           TaskPriority priority, TaskList taskList) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        task.setTaskList(taskList);
        return task;
    }
}