package com.matekoncz.task_manager.exceptions.task;

public class TaskCanNotBeCreatedException extends TaskException {
    public TaskCanNotBeCreatedException() {
        super("Task can not be created.");
    }
    
}
