package com.jefersondev.tasks.integration;

import com.jefersondev.tasks.domain.entities.Task;
import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.TaskPriority;
import com.jefersondev.tasks.domain.entities.TaskStatus;
import com.jefersondev.tasks.domain.entities.dto.TaskDto;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("TasksController — Integration Tests")
class TasksControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;

    private TaskList taskList;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskListRepository.deleteAll();
        taskList = taskListRepository.save(buildTaskList("My List"));
    }


    @Test
    @DisplayName("GET /tasks — should return 200 with empty page")
    void listTasks_shouldReturn200WithEmptyPage() {
        ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalElements")).isEqualTo(0);
    }

    @Test
    @DisplayName("GET /tasks — should return 200 with tasks")
    void listTasks_shouldReturn200WithTasks() {
        taskRepository.save(buildTask("Task A", taskList));
        taskRepository.save(buildTask("Task B", taskList));

        ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalElements")).isEqualTo(2);
    }

    @Test
    @DisplayName("GET /tasks — should filter by status")
    void listTasks_shouldFilterByStatus() {
        Task open = buildTask("Open Task", taskList);
        open.setStatus(TaskStatus.OPEN);
        Task closed = buildTask("Closed Task", taskList);
        closed.setStatus(TaskStatus.CLOSED);
        taskRepository.save(open);
        taskRepository.save(closed);

        ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks?status=OPEN",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("totalElements")).isEqualTo(1);
    }

    @Test
    @DisplayName("GET /tasks — should filter by title (case-insensitive)")
    void listTasks_shouldFilterByTitle() {
        taskRepository.save(buildTask("Buy groceries", taskList));
        taskRepository.save(buildTask("Call dentist", taskList));

        ResponseEntity<Map<String, Object>> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks?title=buy",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("totalElements")).isEqualTo(1);
    }


    @Test
    @DisplayName("POST /tasks — should return 201 when valid")
    void createTask_shouldReturn201WhenValid() {
        TaskDto request = new TaskDto(
                null, "New Task", "Some description",
                null, null, TaskPriority.HIGH,
                null, null, null
        );

        ResponseEntity<TaskDto> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.POST,
                new HttpEntity<>(request),
                TaskDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("New Task");
        assertThat(response.getBody().status()).isEqualTo(TaskStatus.OPEN);
        assertThat(response.getBody().priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.getBody().taskListId()).isEqualTo(taskList.getId());
    }

    @Test
    @DisplayName("POST /tasks — should default priority to MEDIUM when not provided")
    void createTask_shouldDefaultPriorityToMedium() {
        TaskDto request = new TaskDto(
                null, "No Priority Task", null,
                null, null, null,
                null, null, null
        );

        ResponseEntity<TaskDto> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.POST,
                new HttpEntity<>(request),
                TaskDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    @DisplayName("POST /tasks — should return 400 when title is blank")
    void createTask_shouldReturn400WhenTitleBlank() {
        TaskDto request = new TaskDto(
                null, "", null,
                null, null, TaskPriority.LOW,
                null, null, null
        );

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.POST,
                new HttpEntity<>(request),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /tasks — should return 400 when title is null")
    void createTask_shouldReturn400WhenTitleNull() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"description\": \"no title\"}";

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /tasks — should return 400 when task already has an ID")
    void createTask_shouldReturn400WhenIdProvided() {
        TaskDto request = new TaskDto(
                UUID.randomUUID(), "Task with ID", null,
                null, null, TaskPriority.LOW,
                null, null, null
        );

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks",
                HttpMethod.POST,
                new HttpEntity<>(request),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /tasks — should return 404 when task list does not exist")
    void createTask_shouldReturn404WhenTaskListNotFound() {
        TaskDto request = new TaskDto(
                null, "Orphan Task", null,
                null, null, TaskPriority.LOW,
                null, null, null
        );

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + UUID.randomUUID() + "/tasks",
                HttpMethod.POST,
                new HttpEntity<>(request),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /tasks/{id} — should return 200 when found")
    void getTask_shouldReturn200WhenFound() {
        Task saved = taskRepository.save(buildTask("Find Me", taskList));

        ResponseEntity<TaskDto> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + saved.getId(),
                HttpMethod.GET,
                null,
                TaskDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(saved.getId());
        assertThat(response.getBody().title()).isEqualTo("Find Me");
    }

    @Test
    @DisplayName("GET /tasks/{id} — should return 404 when task not found")
    void getTask_shouldReturn404WhenNotFound() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + UUID.randomUUID(),
                HttpMethod.GET,
                null,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("GET /tasks/{id} — should return 404 when task belongs to another list")
    void getTask_shouldReturn404WhenTaskBelongsToAnotherList() {
        TaskList otherList = taskListRepository.save(buildTaskList("Other List"));
        Task task = taskRepository.save(buildTask("Other Task", otherList));

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + task.getId(),
                HttpMethod.GET,
                null,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @DisplayName("PUT /tasks/{id} — should return 200 when valid")
    void updateTask_shouldReturn200WhenValid() {
        Task saved = taskRepository.save(buildTask("Old Title", taskList));

        TaskDto update = new TaskDto(
                saved.getId(), "Updated Title", "Updated desc",
                null, TaskStatus.COMPLETED, TaskPriority.LOW,
                null, null, null
        );

        ResponseEntity<TaskDto> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + saved.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(update),
                TaskDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Updated Title");
        assertThat(response.getBody().status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.getBody().priority()).isEqualTo(TaskPriority.LOW);
    }

    @Test
    @DisplayName("PUT /tasks/{id} — should return 400 when ID in body does not match URL")
    void updateTask_shouldReturn400WhenIdMismatch() {
        Task saved = taskRepository.save(buildTask("Task", taskList));

        TaskDto update = new TaskDto(
                UUID.randomUUID(), "Updated Title", null,
                null, TaskStatus.OPEN, TaskPriority.MEDIUM,
                null, null, null
        );

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + saved.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(update),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("PUT /tasks/{id} — should return 400 when body has no ID")
    void updateTask_shouldReturn400WhenBodyHasNoId() {
        Task saved = taskRepository.save(buildTask("Task", taskList));

        TaskDto update = new TaskDto(
                null, "Updated Title", null,
                null, TaskStatus.OPEN, TaskPriority.MEDIUM,
                null, null, null
        );

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + saved.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(update),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("PUT /tasks/{id} — should return 404 when task not found")
    void updateTask_shouldReturn404WhenNotFound() {
        UUID randomId = UUID.randomUUID();

        TaskDto update = new TaskDto(
                randomId, "Title", null,
                null, TaskStatus.OPEN, TaskPriority.MEDIUM,
                null, null, null
        );

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + randomId,
                HttpMethod.PUT,
                new HttpEntity<>(update),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @DisplayName("DELETE /tasks/{id} — should return 204 when found")
    void deleteTask_shouldReturn204WhenFound() {
        Task saved = taskRepository.save(buildTask("To Delete", taskList));

        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + saved.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /tasks/{id} — should return 404 when task not found")
    void deleteTask_shouldReturn404WhenNotFound() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + UUID.randomUUID(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE /tasks/{id} — should return 404 when task belongs to another list")
    void deleteTask_shouldReturn404WhenTaskBelongsToAnotherList() {
        TaskList otherList = taskListRepository.save(buildTaskList("Other List"));
        Task task = taskRepository.save(buildTask("Other Task", otherList));

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + taskList.getId() + "/tasks/" + task.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    private TaskList buildTaskList(String title) {
        TaskList tl = new TaskList();
        tl.setTitle(title);
        tl.setDescription("Description of " + title);
        return tl;
    }

    private Task buildTask(String title, TaskList list) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description of " + title);
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(TaskPriority.MEDIUM);
        task.setTaskList(list);
        return task;
    }
}