package org.example.vvpd.task.model;

import java.time.Instant;
import java.util.UUID;

public class Task {

    private final UUID id;
    private final String title;
    private final String description;
    private final TaskPriority priority;
    private final Instant createdAt;
    private TaskStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private String result;
    private String error;

    public Task(UUID id, String title, String description, TaskPriority priority, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
        this.status = TaskStatus.NEW;
    }

    public synchronized TaskSnapshot snapshot() {
        return new TaskSnapshot(
                id,
                title,
                description,
                priority,
                status,
                createdAt,
                startedAt,
                completedAt,
                result,
                error
        );
    }

    public synchronized boolean markPending() {
        if (status != TaskStatus.NEW) {
            return false;
        }
        status = TaskStatus.PENDING;
        return true;
    }

    public synchronized boolean markInProgress(Instant now) {
        if (status != TaskStatus.PENDING) {
            return false;
        }
        status = TaskStatus.IN_PROGRESS;
        startedAt = now;
        return true;
    }

    public synchronized boolean complete(String result, Instant now) {
        if (status != TaskStatus.IN_PROGRESS) {
            return false;
        }
        status = TaskStatus.COMPLETED;
        this.result = result;
        completedAt = now;
        return true;
    }

    public synchronized boolean fail(String error, Instant now) {
        if (status != TaskStatus.IN_PROGRESS) {
            return false;
        }
        status = TaskStatus.FAILED;
        this.error = error;
        completedAt = now;
        return true;
    }

    public synchronized boolean cancel(Instant now) {
        if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED || status == TaskStatus.CANCELLED) {
            return false;
        }
        status = TaskStatus.CANCELLED;
        completedAt = now;
        return true;
    }
}
