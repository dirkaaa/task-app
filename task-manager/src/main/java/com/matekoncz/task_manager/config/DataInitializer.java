package com.matekoncz.task_manager.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.user.UserService;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserService userService) {
        return args -> {
            if (userService.getAllUsers().size() == 0) {
                User admin = new User(null, "admin", "admin");
                userService.createUser(admin);
            }
        };
    }
}
