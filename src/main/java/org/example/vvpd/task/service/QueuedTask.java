package org.example.vvpd.task.service;

import java.time.Instant;
import java.util.UUID;
import org.example.vvpd.task.model.TaskPriority;

record QueuedTask(
        UUID taskId,
        TaskPriority priority,
        Instant createdAt,
        long sequence
) implements Comparable<QueuedTask> {

    @Override
    public int compareTo(QueuedTask other) {
        int priorityComparison = Integer.compare(other.priority.weight(), priority.weight());
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        int createdComparison = createdAt.compareTo(other.createdAt);
        if (createdComparison != 0) {
            return createdComparison;
        }
        return Long.compare(sequence, other.sequence);
    }
}
