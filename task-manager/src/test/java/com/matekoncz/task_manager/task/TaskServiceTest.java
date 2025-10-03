package com.matekoncz.task_manager.task;

import com.matekoncz.task_manager.model.Category;
import com.matekoncz.task_manager.service.category.CategoryService;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeUpdatedException;
import com.matekoncz.task_manager.exceptions.task.TaskNotFoundException;
import com.matekoncz.task_manager.exceptions.user.UserNotFoundException;
import com.matekoncz.task_manager.model.Priority;
import com.matekoncz.task_manager.model.Status;
import com.matekoncz.task_manager.model.Task;
import com.matekoncz.task_manager.model.User;
import com.matekoncz.task_manager.repository.TaskRepository;
import com.matekoncz.task_manager.service.task.SearchResult;
import com.matekoncz.task_manager.service.task.TaskService;
import com.matekoncz.task_manager.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private CategoryService categoryService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private User creator;
    private User assignee;
    private List<Task> allTasks;
    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        creator = new User(1L, "creator", "password");
        assignee = new User(2L, "assignee", "password");

        defaultCategory = new Category("Default");
        defaultCategory.setId(1L);
        allTasks = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Task t = new Task(
                    (long) i,
                    "desc" + i,
                    Status.values()[i % Status.values().length],
                    (i % 2 == 0) ? assignee : null,
                    creator,
                    LocalDate.of(2025, 10, (i % 28) + 1),
                    LocalDate.of(2025, 9, (i % 28) + 1),
                    Priority.values()[i % Priority.values().length],
                    defaultCategory);
            allTasks.add(t);
        }
    }

    @Test
    void shouldThrowExceptionIfCategoryDoesNotExist() {
        Category category = new Category("TestCat");
        category.setId(99L);
        Task task = new Task(null, "desc", Status.NEW, null, assignee, LocalDate.now(), LocalDate.now(), Priority.BASIC,
                category);
        when(categoryService.getCategoryById(99L)).thenReturn(null);
        assertThrows(TaskCanNotBeCreatedException.class, () -> taskService.createTask(task));
    }

    @Test
    void shouldCreateTaskWithValidCategory() throws TaskCanNotBeCreatedException {
        Category category = new Category("TestCat");
        category.setId(1L);
        Task task = new Task(null, "desc", Status.NEW, null, assignee, LocalDate.now(), LocalDate.now(), Priority.BASIC,
                category);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        Task result = taskService.createTask(task);
        assertNotNull(result);
        assertEquals("desc", result.getDescription());
    }

    @Test
    void shouldCreateTask() throws TaskCanNotBeCreatedException {
        Task task = new Task(null, "desc", Status.NEW, null, assignee, LocalDate.now(), LocalDate.now(),
                Priority.BASIC, defaultCategory);
        Task savedTask = new Task(1L, "desc", Status.NEW, null, assignee, LocalDate.now(), LocalDate.now(),
                Priority.BASIC, defaultCategory);

        when(taskRepository.save(ArgumentMatchers.any(Task.class))).thenReturn(savedTask);
        when(categoryService.getCategoryById(1L)).thenReturn(defaultCategory);

        Task result = taskService.createTask(task);

        assertNotNull(result);
        assertEquals("desc", result.getDescription());
    }

    @Test
    void shouldGetTaskById() throws TaskNotFoundException {
        Task task = new Task(1L, "desc", null, null, assignee, LocalDate.now(), LocalDate.now(), Priority.BASIC,
                defaultCategory);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals("desc", result.getDescription());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void shouldUpdateTask() throws TaskNotFoundException, UserNotFoundException, TaskCanNotBeUpdatedException {
        Task existingTask = new Task(1L, "desc", Status.NEW, assignee, creator, LocalDate.now(), LocalDate.now(),
                Priority.BASIC, defaultCategory);
        Task updatedTask = new Task(1L, "new desc", Status.NEW, assignee, creator, LocalDate.now(), LocalDate.now(),
                Priority.BASIC, defaultCategory);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(categoryService.getCategoryById(1L)).thenReturn(defaultCategory);

        Task result = taskService.updateTask(1L, updatedTask);

        assertNotNull(result);
        assertEquals("new desc", result.getDescription());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenUpdatingNonexistentTask() {
        Task updatedTask = new Task(1L, "desc", null, null, assignee, LocalDate.now(), LocalDate.now(), Priority.BASIC,
                defaultCategory);
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, updatedTask));
    }

    @Test
    void shouldDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        assertDoesNotThrow(() -> taskService.deleteTask(1L));
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenDeletingNonexistentTask() {
        when(taskRepository.existsById(1L)).thenReturn(false);
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
    }

    // --- Filtering and sorting tests ---

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByStatus() {
        Task filter = new Task();
        filter.setStatus(Status.NEW);

        List<Task> filtered = allTasks.stream()
                .filter(t -> t.getStatus() == Status.NEW)
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> t.getStatus() == Status.NEW));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByDueDate() {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setDueDate(dueDate);

        List<Task> filtered = allTasks.stream()
                .filter(t -> dueDate.equals(t.getDueDate()))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> dueDate.equals(t.getDueDate())));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByAssignee() {
        Task filter = new Task();
        filter.setAssignee(assignee);

        List<Task> filtered = allTasks.stream()
                .filter(t -> assignee.equals(t.getAssignee()))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> assignee.equals(t.getAssignee())));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByCreator() {
        Task filter = new Task();
        filter.setCreator(creator);

        List<Task> filtered = allTasks.stream()
                .filter(t -> creator.equals(t.getCreator()))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> creator.equals(t.getCreator())));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByCategory() {
        Task filter = new Task();
        filter.setCategory(defaultCategory);

        List<Task> filtered = allTasks.stream()
                .filter(t -> defaultCategory.equals(t.getCategory()))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> defaultCategory.equals(t.getCategory())));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByDescription() {
        Task filter = new Task();
        filter.setDescription("desc1");

        List<Task> filtered = allTasks.stream()
                .filter(t -> t.getDescription().toLowerCase().contains("desc1"))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> t.getDescription().toLowerCase().contains("desc1")));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByAllFieldsCombined() {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        filter.setDueDate(dueDate);
        filter.setAssignee(assignee);
        filter.setCreator(creator);
        filter.setDescription("desc");

        List<Task> filtered = allTasks.stream()
                .filter(t -> t.getStatus() == Status.NEW)
                .filter(t -> dueDate.equals(t.getDueDate()))
                .filter(t -> assignee.equals(t.getAssignee()))
                .filter(t -> creator.equals(t.getCreator()))
                .filter(t -> t.getDescription().toLowerCase().contains("desc"))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertEquals(filtered.size(), result.getTasks().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortByDueDateAscending() {
        List<Task> sorted = allTasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(sorted));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "due_date", true);
        assertEquals(sorted.get(0).getDueDate(), result.getTasks().get(0).getDueDate());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortByDueDateDescending() {
        List<Task> sorted = allTasks.stream()
                .sorted(Comparator.comparing(Task::getDueDate).reversed())
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(sorted));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "due_date", false);
        assertEquals(sorted.get(0).getDueDate(), result.getTasks().get(0).getDueDate());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortByStatusAscending() {
        List<Task> sorted = allTasks.stream()
                .sorted(Comparator.comparing(Task::getStatus))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(sorted));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "status", true);
        assertEquals(sorted.get(0).getStatus(), result.getTasks().get(0).getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortByStatusDescending() {
        List<Task> sorted = allTasks.stream()
                .sorted(Comparator.comparing(Task::getStatus).reversed())
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(sorted));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "status", false);
        assertEquals(sorted.get(0).getStatus(), result.getTasks().get(0).getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortAndFilterCombined() {
        LocalDate dueDate = LocalDate.of(2025, 10, 5);
        Task filter = new Task();
        filter.setStatus(Status.NEW);
        filter.setDueDate(dueDate);
        filter.setAssignee(assignee);
        filter.setCreator(creator);
        filter.setDescription("desc");

        List<Task> filteredSorted = allTasks.stream()
                .filter(t -> t.getStatus() == Status.NEW)
                .filter(t -> dueDate.equals(t.getDueDate()))
                .filter(t -> assignee.equals(t.getAssignee()))
                .filter(t -> creator.equals(t.getCreator()))
                .filter(t -> t.getDescription().toLowerCase().contains("desc"))
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filteredSorted));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "due_date", true);
        assertEquals(filteredSorted.size(), result.getTasks().size());
        if (!result.getTasks().isEmpty()) {
            assertEquals(filteredSorted.get(0).getDueDate(), result.getTasks().get(0).getDueDate());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnOnlyTenTasksAndCorrectTotalForPaging() {
        List<Task> tenTasks = allTasks.subList(0, 10);

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(tenTasks, PageRequest.of(0, 10), allTasks.size()));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "", true);
        assertEquals(10, result.getTasks().size());
        assertEquals(allTasks.size(), result.getNumberOfResults());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFilterByPriority() {
        Task filter = new Task();
        filter.setPriority(Priority.HIGH);

        List<Task> filtered = allTasks.stream()
                .filter(t -> t.getPriority() == Priority.HIGH)
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(filtered));

        SearchResult result = taskService.listTaskByFilter(filter, 0, "", true);
        assertTrue(result.getTasks().stream().allMatch(t -> t.getPriority() == Priority.HIGH));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortByPriorityAscending() {
        List<Task> sorted = allTasks.stream()
                .sorted(Comparator.comparing(Task::getPriority))
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(sorted));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "priority", true);
        assertEquals(sorted.get(0).getPriority(), result.getTasks().get(0).getPriority());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSortByPriorityDescending() {
        List<Task> sorted = allTasks.stream()
                .sorted(Comparator.comparing(Task::getPriority).reversed())
                .collect(Collectors.toList());

        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(sorted));

        SearchResult result = taskService.listTaskByFilter(new Task(), 0, "priority", false);
        assertEquals(sorted.get(0).getPriority(), result.getTasks().get(0).getPriority());
    }
}