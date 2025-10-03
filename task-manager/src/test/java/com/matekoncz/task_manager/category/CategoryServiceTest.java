package com.matekoncz.task_manager.category;

import com.matekoncz.task_manager.service.category.CategoryService;
import com.matekoncz.task_manager.exceptions.category.CategoryNotFoundException;
import com.matekoncz.task_manager.model.Category;
import com.matekoncz.task_manager.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category("Work");
        category1.setId(1L);
        category2 = new Category("Personal");
        category2.setId(2L);
    }

    @Test
    void testCreateCategory() {
        when(categoryRepository.save(category1)).thenReturn(category1);
        Category result = categoryService.createCategory(category1);
        assertEquals(category1, result);
    }

    @Test
    void testGetCategoryById() throws CategoryNotFoundException {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        Category result = categoryService.getCategoryById(1L);
        assertEquals(category1, result);
    }

    @Test
    void testGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));
        List<Category> result = categoryService.getAllCategories();
        assertEquals(2, result.size());
    }

    @Test
    void testDeleteCategory() {
        doNothing().when(categoryRepository).deleteById(1L);
        categoryService.deleteCategory(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }
}
