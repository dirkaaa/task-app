package com.matekoncz.task_manager.model;

import java.util.List;

public class SearchResult {
    private long numberOfResults;
    private List<Task> tasks;

    public SearchResult() {
    }

    public SearchResult(long numberOfResults, List<Task> tasks) {
        this.numberOfResults = numberOfResults;
        this.tasks = tasks;
    }

    public long getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
