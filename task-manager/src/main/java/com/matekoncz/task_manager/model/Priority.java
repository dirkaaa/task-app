package com.matekoncz.task_manager.model;

public enum Priority {
    LOW,
    BASIC,
    HIGH,
    CRITICAL;

    public static Priority fromString(String value) {
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}