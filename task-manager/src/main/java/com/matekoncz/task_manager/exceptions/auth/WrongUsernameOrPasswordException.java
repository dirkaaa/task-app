package com.matekoncz.task_manager.exceptions.auth;

public class WrongUsernameOrPasswordException extends Exception {
    public WrongUsernameOrPasswordException() {
        super("Wrong username or password.");
    }
}