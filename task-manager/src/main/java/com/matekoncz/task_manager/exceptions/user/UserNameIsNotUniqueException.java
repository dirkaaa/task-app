package com.matekoncz.task_manager.exceptions.user;

public class UserNameIsNotUniqueException extends UserException {
    public UserNameIsNotUniqueException() {
        super("Username is not unique.");
    }
}