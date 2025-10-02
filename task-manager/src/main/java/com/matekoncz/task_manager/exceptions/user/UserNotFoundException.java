package com.matekoncz.task_manager.exceptions.user;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super("User not found.");
    }
}