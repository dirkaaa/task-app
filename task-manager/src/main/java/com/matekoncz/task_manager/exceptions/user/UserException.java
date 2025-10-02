package com.matekoncz.task_manager.exceptions.user;

public abstract class UserException extends Exception {
    public UserException(String message) {
        super(message);
    }
}
