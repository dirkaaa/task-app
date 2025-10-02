package com.matekoncz.task_manager.user;

import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.exceptions.user.UserNotFoundException;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.task.TaskService;
import com.matekoncz.task_manager.service.user.Credentials;
import com.matekoncz.task_manager.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends TaskManagerIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() throws Exception {
        taskService.deleteAll();
        userService.deleteAll();
        User creator = new User(null, "creator", "password");
        Credentials credentials = Credentials.of(creator);
        userService.createUser(creator);

        ResponseEntity<User> loginResponse = restTemplate.postForEntity("/api/auth/login", credentials, User.class);
        headers = new HttpHeaders();
        headers.set("Cookie", loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
    }

    @Test
    void shouldCreateUser() throws UserNotFoundException {
        User user = new User(null, "testuser", "password");
        HttpEntity<User> entity = new HttpEntity<>(user, headers);

        ResponseEntity<User> response = restTemplate.postForEntity("/api/users/register", entity, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());

        User created = userService.getUserByUsername("testuser");
        assertNotNull(created);
        assertEquals("testuser", created.getUsername());
    }

    @Test
    void shouldListAllUsers() throws UserCanNotBeCreatedException, UserNameIsNotUniqueException {
        userService.createUser(new User(null, "user2", "pass2"));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<User[]> response = restTemplate.exchange("/api/users/all", HttpMethod.GET, entity, User[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("creator")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("user2")));
    }
}