package com.matekoncz.task_manager.auth;

import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.model.Credentials;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest extends TaskManagerIntegrationTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() throws UserCanNotBeCreatedException, UserNameIsNotUniqueException {
        userService.deleteAll();
        userService.createUser(new User(null, "authuser", "password"));
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        String credentialsJson = "{\"username\":\"authuser\",\"password\":\"password\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authuser"));
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        String credentialsJson = "{\"username\":\"authuser\",\"password\":\"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentialsJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        login(new Credentials("authuser", "password"));        

        mockMvc.perform(delete("/api/auth/logout").session(session))
                .andExpect(status().isOk());
    }
}