package com.matekoncz.task_manager.category;

import com.matekoncz.task_manager.TaskManagerIntegrationTest;
import com.matekoncz.task_manager.model.Category;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.service.category.CategoryService;
import com.matekoncz.task_manager.service.user.Credentials;
import com.matekoncz.task_manager.service.user.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class CategoryControllerTest extends TaskManagerIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        userService.deleteAll();
        categoryService.deleteAll();
        User creator = new User(null, "creator", "password");
        Credentials credentials = Credentials.of(creator);
        userService.createUser(creator);

        ResponseEntity<User> loginResponse = restTemplate.postForEntity("/api/auth/login", credentials, User.class);
        headers = new HttpHeaders();
        headers.set("Cookie", loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
    }

    @Test
    void shouldCreateCategory() {
        Category category = new Category("Test Category");
        HttpEntity<Category> entity = new HttpEntity<>(category, headers);
        ResponseEntity<Category> response = restTemplate.postForEntity("/api/categories", entity, Category.class);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldListEveryCategory() {
        Category category1 = new Category("Category 1");
        Category category2 = new Category("Category 2");

        categoryService.createCategory(category1);
        categoryService.createCategory(category2);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Category[]> response = restTemplate.exchange("/api/categories",
                org.springframework.http.HttpMethod.GET, entity, Category[].class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);

    }

    @Test
    void shouldDeleteCategory() {
        Category category = new Category("To Be Deleted");
        Category savedCategory = categoryService.createCategory(category);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange("/api/categories/" + savedCategory.getId(),
                org.springframework.http.HttpMethod.DELETE, entity, Void.class);

        assertEquals(204, response.getStatusCode().value());
        assertEquals(0, categoryService.getAllCategories().size());

    }
}
