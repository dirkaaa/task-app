package com.matekoncz.task_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matekoncz.task_manager.model.Task;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.task.SearchResult;
import com.matekoncz.task_manager.service.task.TaskService;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeUpdatedException;
import com.matekoncz.task_manager.exceptions.task.TaskNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    public TaskController(TaskService taskService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) throws TaskNotFoundException {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/all")
    public ResponseEntity<Iterable<Task>> getAllTasks() {
        Iterable<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/all")
    public ResponseEntity<SearchResult> listTasksByFilter(@RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "") String orderBy,
            @RequestParam(defaultValue = "true") boolean ascending,
            @RequestBody String filterTaskJson) throws IOException {
        Task filterTask = objectMapper.readValue(filterTaskJson, Task.class);
        SearchResult searchResult = taskService.listTaskByFilter(filterTask, offset, orderBy, ascending);
        return ResponseEntity.ok(searchResult);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(Authentication authentication, @RequestBody String taskJson)
            throws IOException, TaskCanNotBeCreatedException {
        Task task = objectMapper.readValue(taskJson, Task.class);
        task.setCreator((User) authentication.getPrincipal());
        task.setCreatedAt(LocalDate.now());
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody String taskJson)
            throws IOException, TaskNotFoundException, TaskCanNotBeUpdatedException {
        Task updatedTask = objectMapper.readValue(taskJson, Task.class);
        Task task = taskService.updateTask(id, updatedTask);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) throws TaskNotFoundException {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
