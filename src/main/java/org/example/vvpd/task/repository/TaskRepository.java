package org.example.vvpd.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.vvpd.task.model.Task;
import org.example.vvpd.task.model.TaskSnapshot;

public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findById(UUID id);

    List<TaskSnapshot> findAllSnapshots();
}
