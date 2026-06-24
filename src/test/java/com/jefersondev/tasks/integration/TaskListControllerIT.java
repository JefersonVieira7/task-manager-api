package com.jefersondev.tasks.integration;

import com.jefersondev.tasks.domain.entities.TaskList;
import com.jefersondev.tasks.domain.entities.dto.TaskListDto;
import com.jefersondev.tasks.domain.entities.dto.AuthResponse;
import com.jefersondev.tasks.domain.entities.dto.RegisterRequest;
import com.jefersondev.tasks.repositories.TaskListRepository;
import com.jefersondev.tasks.repositories.UserRepository;
import com.jefersondev.tasks.services.AuthService;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisplayName("TaskListController — Integration Tests")
class TaskListControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private String token;

    @BeforeEach
    void setUp() {
        taskListRepository.deleteAll();
        userRepository.deleteAll();

        var register = new RegisterRequest("Test User", "test@test.com", "password123");
        AuthResponse auth = authService.register(register);
        token = auth.token();
    }

    private <T> HttpEntity<T> authEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> authEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    @DisplayName("GET /api/task-lists — should return 200 with empty list")
    void listTaskLists_shouldReturn200WithEmptyList() {
        ResponseEntity<List<TaskListDto>> response = testRestTemplate.exchange(
                "/api/task-lists",
                HttpMethod.GET,
                authEntity(),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("GET /api/task-lists — should return 200 with data")
    void listTaskLists_shouldReturn200WithData() {
        taskListRepository.save(buildTaskList("Work"));
        taskListRepository.save(buildTaskList("Personal"));

        ResponseEntity<List<TaskListDto>> response = testRestTemplate.exchange(
                "/api/task-lists",
                HttpMethod.GET,
                authEntity(),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(2);
        assertThat(response.getBody())
                .extracting(TaskListDto::title)
                .containsExactlyInAnyOrder("Work", "Personal");
    }

    @Test
    @DisplayName("POST /api/task-lists — should return 201 when valid")
    void createTaskList_shouldReturn201WhenValid() {
        TaskListDto request = new TaskListDto(null, "New List", "My description", null, null, null);

        ResponseEntity<TaskListDto> response = testRestTemplate.exchange(
                "/api/task-lists",
                HttpMethod.POST,
                authEntity(request),
                TaskListDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("New List");
    }

    @Test
    @DisplayName("POST /api/task-lists — should return 400 when title is blank")
    void createTaskList_shouldReturn400WhenTitleBlank() {
        TaskListDto request = new TaskListDto(null, "", null, null, null, null);

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists",
                HttpMethod.POST,
                authEntity(request),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/task-lists — should return 400 when title is null")
    void createTaskList_shouldReturn400WhenTitleNull() {
        String body = "{\"description\": \"no title\"}";

        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists",
                HttpMethod.POST,
                authEntity(body),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /api/task-lists/{id} — should return 200 when found")
    void getTaskList_shouldReturn200WhenFound() {
        TaskList saved = taskListRepository.save(buildTaskList("Found List"));

        ResponseEntity<TaskListDto> response = testRestTemplate.exchange(
                "/api/task-lists/" + saved.getId(),
                HttpMethod.GET,
                authEntity(),
                TaskListDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Found List");
        assertThat(response.getBody().id()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("GET /api/task-lists/{id} — should return 404 when not found")
    void getTaskList_shouldReturn404WhenNotFound() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + UUID.randomUUID(),
                HttpMethod.GET,
                authEntity(),
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("PUT /api/task-lists/{id} — should return 200 when valid")
    void updateTaskList_shouldReturn200WhenValid() {
        TaskList saved = taskListRepository.save(buildTaskList("Old Title"));

        TaskListDto update = new TaskListDto(saved.getId(), "New Title", "Updated desc", null, null, null);

        ResponseEntity<TaskListDto> response = testRestTemplate.exchange(
                "/api/task-lists/" + saved.getId(),
                HttpMethod.PUT,
                authEntity(update),
                TaskListDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("DELETE /api/task-lists/{id} — should return 204 when found")
    void deleteTaskList_shouldReturn204WhenFound() {
        TaskList saved = taskListRepository.save(buildTaskList("To Delete"));

        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/task-lists/" + saved.getId(),
                HttpMethod.DELETE,
                authEntity(),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(taskListRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/task-lists/{id} — should return 404 when not found")
    void deleteTaskList_shouldReturn404WhenNotFound() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/api/task-lists/" + UUID.randomUUID(),
                HttpMethod.DELETE,
                authEntity(),
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
}