package com.matekoncz.task_manager.repository;

import com.matekoncz.task_manager.model.Category;
import com.matekoncz.task_manager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Task> {
}