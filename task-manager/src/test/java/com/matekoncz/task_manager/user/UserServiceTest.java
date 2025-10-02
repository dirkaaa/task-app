package com.matekoncz.task_manager.user;

import com.matekoncz.task_manager.exceptions.auth.WrongUsernameOrPasswordException;
import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.exceptions.user.UserNotFoundException;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.repository.UserRepository;
import com.matekoncz.task_manager.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void shouldCreateUser() throws UserCanNotBeCreatedException, UserNameIsNotUniqueException {
        User user = new User(null, "testuser", "password");
        User savedUser = new User(1L, "testuser", passwordEncoder.encode("password"));

        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertTrue(passwordEncoder.matches("password", result.getPassword()));
    }

    @Test
    void shouldGetUserById() throws UserNotFoundException {
        User user = new User(1L, "testuser", "password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundById() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void shouldGetUserByUsername() throws UserNotFoundException {
        User user = new User(1L, "testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserNotFoundByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("testuser"));
    }

    @Test
    void shouldAuthenticateWithCorrectCredentials() throws UserNotFoundException, WrongUsernameOrPasswordException {
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(1L, "testuser", encodedPassword);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.authenticate("testuser", rawPassword);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenAuthenticatingNonexistentUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.authenticate("testuser", "password"));
    }

    @Test
    void shouldThrowWrongUsernameOrPasswordExceptionWhenPasswordDoesNotMatch() {
        String encodedPassword = passwordEncoder.encode("password");
        User user = new User(1L, "testuser", encodedPassword);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertThrows(WrongUsernameOrPasswordException.class, () -> userService.authenticate("testuser", "wrongpassword"));
    }

    @Test
    void shouldGetAllUsers() {
        List<User> users = Arrays.asList(
                new User(1L, "user1", "pass1"),
                new User(2L, "user2", "pass2")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }
}