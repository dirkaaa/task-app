package com.matekoncz.task_manager.exceptions.task;

public class TaskNotFoundException extends TaskException {
    public TaskNotFoundException() {
        super("Task not found.");
    }
}