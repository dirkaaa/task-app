package com.matekoncz.task_manager.exceptions.task;

public class TaskCanNotBeUpdatedException extends TaskException {
    public TaskCanNotBeUpdatedException() {
        super("Task can not be updated.");
    }
}