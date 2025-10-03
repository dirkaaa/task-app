package com.matekoncz.task_manager.controller.exceptions;

import com.matekoncz.task_manager.exceptions.user.UserCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.user.UserNameIsNotUniqueException;
import com.matekoncz.task_manager.exceptions.user.UserNotFoundException;
import com.matekoncz.task_manager.exceptions.auth.WrongUsernameOrPasswordException;
import com.matekoncz.task_manager.exceptions.category.CategoryNotFoundException;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeUpdatedException;
import com.matekoncz.task_manager.exceptions.task.TaskNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleTaskNotFoundException(TaskNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TaskCanNotBeCreatedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleTaskCannotBeCreatedException(TaskCanNotBeCreatedException ex) {
        return buildResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

    @ExceptionHandler(TaskCanNotBeUpdatedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleTaskCanNotBeUpdatedException(TaskCanNotBeUpdatedException ex) {
        return buildResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WrongUsernameOrPasswordException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleWrongUsernameOrPassword(WrongUsernameOrPasswordException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserCanNotBeCreatedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleUserCannotBeCreatedException(UserCanNotBeCreatedException ex) {
        return buildResponse(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
    }

    @ExceptionHandler(UserNameIsNotUniqueException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleUsernameIsNotUniqueException(UserNameIsNotUniqueException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleOtherExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return new ResponseEntity<>(body, status);
    }
}