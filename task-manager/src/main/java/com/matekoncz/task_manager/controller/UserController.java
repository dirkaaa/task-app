package com.matekoncz.task_manager.controller;

import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.user.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<com.matekoncz.task_manager.model.UserDto> createUser(@RequestBody String userJson)
            throws IOException, UserCanNotBeCreatedException, UserNameIsNotUniqueException {
        User user = objectMapper.readValue(userJson, User.class);
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(userService.toUserDto(createdUser));
    }

    @GetMapping("/all")
    public ResponseEntity<List<com.matekoncz.task_manager.model.UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userService.toUserDtoList(users));
    }
}