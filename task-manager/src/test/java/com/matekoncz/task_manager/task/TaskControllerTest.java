package com.matekoncz.task_manager.task;

import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.exceptions.task.TaskNotFoundException;
import com.matekoncz.task_manager.model.Status;
import com.matekoncz.task_manager.model.Task;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.task.SearchResult;
import com.matekoncz.task_manager.service.task.TaskService;
import com.matekoncz.task_manager.service.user.Credentials;
import com.matekoncz.task_manager.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskControllerTest extends TaskManagerIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private HttpHeaders headers;
    private User creator;
    private User assignee;
    private List<Task> allTasks;

    @BeforeEach
    void setUp() throws Exception {
        taskService.deleteAll();
        userService.deleteAll();
        User unsavedCreator = new User(null, "creator", "password");
        User unsavedAssignee = new User(null, "assignee", "password");
        Credentials credentials = Credentials.of(unsavedCreator);

        creator = userService.createUser(unsavedCreator);
        assignee = userService.createUser(unsavedAssignee);

        ResponseEntity<User> loginResponse = restTemplate.postForEntity("/api/auth/login", credentials, User.class);
        headers = new HttpHeaders();
        headers.set("Cookie", loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE));

        allTasks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Task t = new Task(
                    null,
                    "desc" + i,
                    Status.values()[i % Status.values().length],
                    (i % 2 == 0) ? assignee : creator,
                    creator,
                    LocalDate.of(2025, 10, (i % 28) + 1),
                    LocalDate.of(2025, 9, (i % 28) + 1));
            allTasks.add(taskService.createTask(t));
        }
    }

    @Test
    void shouldCreateTask() {
        Task task = new Task(null, "desc", Status.NEW, assignee, creator, LocalDate.now(), LocalDate.now());
        HttpEntity<Task> entity = new HttpEntity<>(task, headers);

        ResponseEntity<Task> response = restTemplate.postForEntity("/api/tasks", entity, Task.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getDescription(), is("desc"));
    }

    @Test
    void shouldUpdateTask() {
        Task task = allTasks.get(0);
        task.setDescription("updated desc");
        HttpEntity<Task> entity = new HttpEntity<>(task, headers);

        ResponseEntity<Task> response = restTemplate.exchange("/api/tasks/" + task.getId(), HttpMethod.PUT, entity,
                Task.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getDescription(), is("updated desc"));
    }

    @Test
    void shouldDeleteTask() {
        Task task = allTasks.get(0);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange("/api/tasks/" + task.getId(), HttpMethod.DELETE, entity,
                Void.class);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(task.getId()));
    }

    @Test
    void shouldFilterByStatus() {
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("status", is(Status.NEW))));
    }

    @Test
    void shouldFilterByDueDate() {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setDueDate(dueDate);
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("dueDate", is(dueDate))));
    }

    @Test
    void shouldFilterByAssignee() {
        Task filter = new Task();
        filter.setAssignee(assignee);
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(),
                everyItem(hasProperty("assignee", hasProperty("username", is(assignee.getUsername())))));
    }

    @Test
    void shouldFilterByCreator() {
        Task filter = new Task();
        filter.setCreator(creator);
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(),
                everyItem(hasProperty("creator", hasProperty("username", is(creator.getUsername())))));
    }

    @Test
    void shouldFilterByDescription() {
        Task filter = new Task();
        filter.setDescription("desc1");
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("description", containsString("desc1"))));
    }

    @Test
    void shouldFilterByAllFieldsCombined() {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        filter.setDueDate(dueDate);
        filter.setAssignee(assignee);
        filter.setCreator(creator);
        filter.setDescription("desc");
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("status", is(Status.NEW))));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("dueDate", is(dueDate))));
        assertThat(response.getBody().getTasks(),
                everyItem(hasProperty("assignee", hasProperty("username", is(assignee.getUsername())))));
        assertThat(response.getBody().getTasks(),
                everyItem(hasProperty("creator", hasProperty("username", is(creator.getUsername())))));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("description", containsString("desc"))));
    }

    @Test
    void shouldSortByDueDateAscending() {
        Task filter = new Task();
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=dueDate&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks().get(0).getDueDate(), notNullValue());
    }

    @Test
    void shouldSortByDueDateDescending() {
        Task filter = new Task();
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=dueDate&ascending=false", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks().get(0).getDueDate(), notNullValue());
    }

    @Test
    void shouldSortByStatusAscending() {
        Task filter = new Task();
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=status&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks().get(0).getStatus(), notNullValue());
    }

    @Test
    void shouldSortByStatusDescending() {
        Task filter = new Task();
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=status&ascending=false", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks().get(0).getStatus(), notNullValue());
    }

    @Test
    void shouldSortAndFilterCombined() {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        filter.setDueDate(dueDate);
        filter.setAssignee(assignee);
        filter.setCreator(creator);
        filter.setDescription("desc");
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=dueDate&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("status", is(Status.NEW))));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("dueDate", is(dueDate))));
        assertThat(response.getBody().getTasks(),
                everyItem(hasProperty("assignee", hasProperty("username", is(assignee.getUsername())))));
        assertThat(response.getBody().getTasks(),
                everyItem(hasProperty("creator", hasProperty("username", is(creator.getUsername())))));
        assertThat(response.getBody().getTasks(), everyItem(hasProperty("description", containsString("desc"))));
    }

    @Test
    void shouldReturnOnlyTenTasksAndCorrectTotalForPaging() {
        Task filter = new Task();
        HttpEntity<Task> entity = new HttpEntity<>(filter, headers);

        ResponseEntity<SearchResult> response = restTemplate
                .postForEntity("/api/tasks/all?offset=0&orderBy=&ascending=true", entity, SearchResult.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getTasks(), hasSize(10));
        assertThat(response.getBody().getNumberOfResults(), is(25L));
    }
}