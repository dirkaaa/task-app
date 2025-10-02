package com.matekoncz.task_manager.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.model.Credentials;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.TaskService;
import com.matekoncz.task_manager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends TaskManagerIntegrationTest {
    
    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        taskService.deleteAll();
        userService.deleteAll();
        User creator = new User(null, "creator", "password");
        Credentials credentials = Credentials.of(creator);
        userService.createUser(creator);
        login(credentials);
    }

    @Test
    void shouldCreateUser() throws Exception {
        User user = new User(null, "testuser", "password");

        mockMvc.perform(post("/api/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        User created = userService.getUserByUsername("testuser");
        assertNotNull(created);
        assertEquals("testuser", created.getUsername());
    }

    @Test
    void shouldListAllUsers() throws Exception {
        userService.createUser(new User(null, "user2", "pass2"));

        mockMvc.perform(get("/api/users/all").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("creator")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user2")));
    }
}