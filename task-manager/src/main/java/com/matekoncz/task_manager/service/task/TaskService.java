package com.matekoncz.task_manager.service.task;

import com.matekoncz.task_manager.model.Task;
import com.matekoncz.task_manager.repository.TaskRepository;
import com.matekoncz.task_manager.service.user.UserService;
import com.matekoncz.task_manager.service.category.CategoryService;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeCreatedException;
import com.matekoncz.task_manager.exceptions.task.TaskCanNotBeUpdatedException;
import com.matekoncz.task_manager.exceptions.task.TaskNotFoundException;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

@Service
public class TaskService {

    private static final int TASK_BATCH_SIZE = 10;

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("dueDate", "status", "priority");

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public TaskService(TaskRepository taskRepository, UserService userService, CategoryService categoryService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public Task createTask(Task task) throws TaskCanNotBeCreatedException {
        validateTask(task);
        return taskRepository.save(task);
    }

    private void validateTask(Task task) throws TaskCanNotBeCreatedException {
        if (task.getDescription() == null || task.getDescription().isBlank() || task.getStatus() == null
                || task.getPriority() == null) {
            throw new TaskCanNotBeCreatedException();
        }
        try {
            userService.getUserById(task.getCreator().getId());
            if (task.getAssignee() != null) {
                userService.getUserById(task.getAssignee().getId());
            }
            if (task.getCategory() != null) {
                if (categoryService.getCategoryById(task.getCategory().getId()) == null) {
                    throw new TaskCanNotBeCreatedException();
                }
            }
        } catch (Exception e) {
            throw new TaskCanNotBeCreatedException();
        }
    }

    public Task getTaskById(Long id) throws TaskNotFoundException {
        return taskRepository.findById(id)
                .orElseThrow(TaskNotFoundException::new);
    }

    public Task updateTask(Long id, Task updatedTask) throws TaskNotFoundException, TaskCanNotBeUpdatedException {
        Task task = taskRepository.findById(id)
                .orElseThrow(TaskNotFoundException::new);
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setAssignee(updatedTask.getAssignee());
        task.setDueDate(updatedTask.getDueDate());
        task.setPriority(updatedTask.getPriority());
        task.setCategory(updatedTask.getCategory());
        try {
            validateTask(task);
        } catch (TaskCanNotBeCreatedException e) {
            throw new TaskCanNotBeUpdatedException();
        }
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) throws TaskNotFoundException {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException();
        }
        taskRepository.deleteById(id);
    }

    public void deleteAll() {
        taskRepository.deleteAll();
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public SearchResult listTaskByFilter(Task filterTask, int offset, String orderBy, boolean ascending) {
        Pageable pageable;
        if (!orderBy.isBlank() && ALLOWED_SORT_FIELDS.contains(orderBy)) {
            Sort sort = ascending ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
            pageable = getPageable(filterTask, offset, sort);
        } else {
            pageable = getPageable(filterTask, offset);
        }
        return listTasksByFilterAndPageable(filterTask, pageable);
    }

    private Pageable getPageable(Task filterTask, int offset) {
        return getPageable(filterTask, offset, Sort.by("id").ascending());
    }

    private Pageable getPageable(Task filterTask, int offset, Sort sort) {
        int pageNumber = offset / TASK_BATCH_SIZE;
        return PageRequest.of(pageNumber, TASK_BATCH_SIZE, sort);
    }

    private SearchResult listTasksByFilterAndPageable(Task filter, Pageable pageable) {
        Specification<Task> specification = createSecification(filter);
        Page<Task> results = taskRepository.findAll(specification, pageable);
        return new SearchResult(results.getTotalElements(), results.getContent());
    }

    private Specification<Task> createSecification(Task filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus().ordinal()));
            }

            if (filter.getPriority() != null) {
                predicates.add(cb.equal(root.get("priority"), filter.getPriority().ordinal()));
            }

            if (filter.getDueDate() != null) {
                predicates.add(cb.equal(root.get("dueDate"), filter.getDueDate()));
            }

            if (filter.getAssignee() != null) {
                predicates.add(cb.equal(root.get("assignee"), filter.getAssignee()));
            }

            if (filter.getCreator() != null) {
                predicates.add(cb.equal(root.get("creator"), filter.getCreator()));
            }

            if (filter.getDescription() != null && !filter.getDescription().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + filter.getDescription() + "%"));
            }

            if (filter.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}