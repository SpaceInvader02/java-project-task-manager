package org.example.vvpd.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.example.vvpd.task.model.Task;
import org.example.vvpd.task.model.TaskSnapshot;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final ConcurrentMap<UUID, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        tasks.put(task.snapshot().id(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<TaskSnapshot> findAllSnapshots() {
        return tasks.values().stream()
                .map(Task::snapshot)
                .toList();
    }
}
