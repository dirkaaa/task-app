package com.matekoncz.task_manager.exceptions.category;

public class CategoryNotFoundException extends CategoryException {
    public CategoryNotFoundException() {
        super("Category not found.");
    }
}
