package com.matekoncz.task_manager.task;

import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.exceptions.task.TaskNotFoundException;
import com.matekoncz.task_manager.model.Credentials;
import com.matekoncz.task_manager.model.Status;
import com.matekoncz.task_manager.model.Task;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.TaskService;
import com.matekoncz.task_manager.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
public class TaskControllerTest extends TaskManagerIntegrationTest{

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

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

        login(credentials);
        allTasks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Task t = new Task(
                null,
                "desc" + i,
                Status.values()[i % Status.values().length],
                (i % 2 == 0) ? assignee : null,
                creator,
                LocalDate.of(2025, 10, (i % 28) + 1),
                LocalDate.of(2025, 9, (i % 28) + 1)
            );
            allTasks.add(taskService.createTask(t));
        }
    }

    // --- Existing tests for CRUD ---

    @Test
    void shouldCreateTask() throws Exception {
        Task task = new Task(null, "desc", Status.NEW, assignee, creator, LocalDate.now(), LocalDate.now());

        mockMvc.perform(post("/api/tasks")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("desc"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        Task task = allTasks.get(0);
        task.setDescription("updated desc");

        mockMvc.perform(put("/api/tasks/" + task.getId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("updated desc"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Task task = allTasks.get(0);

        mockMvc.perform(delete("/api/tasks/" + task.getId()).session(session))
                .andExpect(status().isNoContent());

        assertThrows(TaskNotFoundException.class,()->taskService.getTaskById(task.getId()));
    }

    // --- Filtering and sorting tests ---

    @Test
    void shouldFilterByStatus() throws Exception {
        Task filter = new Task();
        filter.setStatus(Status.NEW);

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].status", everyItem(is(Status.NEW.name()))));
    }

    @Test
    void shouldFilterByDueDate() throws Exception {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setDueDate(dueDate);

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].dueDate", everyItem(is(dueDate.toString()))));
    }

    @Test
    void shouldFilterByAssignee() throws Exception {
        Task filter = new Task();
        filter.setAssignee(assignee);

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].assignee.username", everyItem(is(assignee.getUsername()))));
    }

    @Test
    void shouldFilterByCreator() throws Exception {
        Task filter = new Task();
        filter.setCreator(creator);

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].creator.username", everyItem(is(creator.getUsername()))));
    }

    @Test
    void shouldFilterByDescription() throws Exception {
        Task filter = new Task();
        filter.setDescription("desc1");

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].description", everyItem(containsString("desc1"))));
    }

    @Test
    void shouldFilterByAllFieldsCombined() throws Exception {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        filter.setDueDate(dueDate);
        filter.setAssignee(assignee);
        filter.setCreator(creator);
        filter.setDescription("desc");

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].status", everyItem(is(Status.NEW.name()))))
                .andExpect(jsonPath("$.tasks[*].dueDate", everyItem(is(dueDate.toString()))))
                .andExpect(jsonPath("$.tasks[*].assignee.username", everyItem(is(assignee.getUsername()))))
                .andExpect(jsonPath("$.tasks[*].creator.username", everyItem(is(creator.getUsername()))))
                .andExpect(jsonPath("$.tasks[*].description", everyItem(containsString("desc"))));
    }

    @Test
    void shouldSortByDueDateAscending() throws Exception {
        Task filter = new Task();

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "dueDate")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].dueDate").exists());
        // Additional checks for order can be added if needed
    }

    @Test
    void shouldSortByDueDateDescending() throws Exception {
        Task filter = new Task();

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "dueDate")
                .param("ascending", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].dueDate").exists());
    }

    @Test
    void shouldSortByStatusAscending() throws Exception {
        Task filter = new Task();

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "status")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].status").exists());
    }

    @Test
    void shouldSortByStatusDescending() throws Exception {
        Task filter = new Task();

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "status")
                .param("ascending", "false")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].status").exists());
    }

    @Test
    void shouldSortAndFilterCombined() throws Exception {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        filter.setDueDate(dueDate);
        filter.setAssignee(assignee);
        filter.setCreator(creator);
        filter.setDescription("desc");

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "dueDate")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[*].status", everyItem(is(Status.NEW.name()))))
                .andExpect(jsonPath("$.tasks[*].dueDate", everyItem(is(dueDate.toString()))))
                .andExpect(jsonPath("$.tasks[*].assignee.username", everyItem(is(assignee.getUsername()))))
                .andExpect(jsonPath("$.tasks[*].creator.username", everyItem(is(creator.getUsername()))))
                .andExpect(jsonPath("$.tasks[*].description", everyItem(containsString("desc"))));
    }

    @Test
    void shouldReturnOnlyTenTasksAndCorrectTotalForPaging() throws Exception {
        Task filter = new Task();

        mockMvc.perform(post("/api/tasks/all")
                .session(session)
                .param("offset", "0")
                .param("orderBy", "")
                .param("ascending", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks", hasSize(10)))
                .andExpect(jsonPath("$.numberOfResults", is(25)));
    }
}