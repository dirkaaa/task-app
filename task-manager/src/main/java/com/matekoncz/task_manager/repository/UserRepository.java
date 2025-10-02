package com.matekoncz.task_manager.repository;

import com.matekoncz.task_manager.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM task_manager_user WHERE username = :username", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);
}