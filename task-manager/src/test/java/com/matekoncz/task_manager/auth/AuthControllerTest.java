package com.matekoncz.task_manager.auth;

import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.user.Credentials;
import com.matekoncz.task_manager.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerTest extends TaskManagerIntegrationTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() throws UserCanNotBeCreatedException, UserNameIsNotUniqueException {
        userService.deleteAll();
        userService.createUser(new User(null, "authuser", "password"));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        Credentials credentials = new Credentials("authuser", "password");
        ResponseEntity<User> response = restTemplate.postForEntity("/api/auth/login", credentials, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("authuser", response.getBody().getUsername());
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() {
        Credentials credentials = new Credentials("authuser", "wrongpassword");
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", credentials, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldLogoutSuccessfully() {
        Credentials credentials = new Credentials("authuser", "password");
        ResponseEntity<User> loginResponse = restTemplate.postForEntity("/api/auth/login", credentials, User.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> logoutResponse = restTemplate.exchange("/api/auth/logout", HttpMethod.DELETE, entity,
                Void.class);

        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
    }
}