package com.matekoncz.task_manager.model;

public enum Status {
    NEW, IN_PROGRESS, COMPLETED, CANCELLED;

    public static Status fromString(String status) {
        for (Status s : Status.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + status);
    }
}
