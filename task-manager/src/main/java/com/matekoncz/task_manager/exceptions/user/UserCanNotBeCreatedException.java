package com.matekoncz.task_manager.exceptions.user;

public class UserCanNotBeCreatedException extends UserException {
    public UserCanNotBeCreatedException() {
        super("User can not be created.");
    }
}