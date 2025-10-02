package com.matekoncz.task_manager.service.user;

import com.matekoncz.task_manager.model.User;

public class Credentials {
    private String username;
    private String password;

    public Credentials() {
    }

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static Credentials of(User creator) {
        return new Credentials(creator.getUsername(), creator.getPassword());
    }
}
