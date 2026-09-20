package org.example.vvpd.task.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;
import org.example.vvpd.config.TaskProcessingProperties;
import org.example.vvpd.task.dto.CreateTaskRequest;
import org.example.vvpd.task.dto.PagedResponse;
import org.example.vvpd.task.exception.TaskNotFoundException;
import org.example.vvpd.task.model.TaskPriority;
import org.example.vvpd.task.model.TaskSnapshot;
import org.example.vvpd.task.model.TaskStatus;
import org.example.vvpd.task.repository.InMemoryTaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

    private TaskService service;

    @AfterEach
    void tearDown() {
        if (service != null) {
            service.shutdown();
        }
    }

    @Test
    void createsTaskAndMovesItToPendingQueue() {
        service = new TaskService(new InMemoryTaskRepository(), new TaskProcessingProperties(1, Duration.ofSeconds(5)));

        TaskSnapshot task = service.create(new CreateTaskRequest("Import", "Import data", TaskPriority.HIGH));

        assertThat(task.id()).isNotNull();
        assertThat(task.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.createdAt()).isNotNull();
    }

    @Test
    void filtersTasksWithPagination() {
        service = new TaskService(new InMemoryTaskRepository(), new TaskProcessingProperties(1, Duration.ofSeconds(5)));
        service.create(new CreateTaskRequest("Low", "Low task", TaskPriority.LOW));
        service.create(new CreateTaskRequest("High", "High task", TaskPriority.HIGH));

        PagedResponse<TaskSnapshot> page = service.findAll(null, TaskPriority.HIGH, 0, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().getFirst().priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void cancelsTaskBeforeCompletion() {
        service = new TaskService(new InMemoryTaskRepository(), new TaskProcessingProperties(1, Duration.ofSeconds(5)));
        TaskSnapshot task = service.create(new CreateTaskRequest("Cancel", "Cancel me", TaskPriority.MEDIUM));

        TaskSnapshot cancelled = service.cancel(task.id());

        assertThat(cancelled.status()).isEqualTo(TaskStatus.CANCELLED);
        assertThat(cancelled.completedAt()).isNotNull();
    }

    @Test
    void completesTaskInBackground() throws Exception {
        service = new TaskService(new InMemoryTaskRepository(), new TaskProcessingProperties(1, Duration.ofMillis(10)));
        TaskSnapshot task = service.create(new CreateTaskRequest("Complete", "Complete me", TaskPriority.MEDIUM));

        TaskSnapshot completed = waitForTerminalStatus(task.id());

        assertThat(completed.status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.startedAt()).isNotNull();
        assertThat(completed.completedAt()).isNotNull();
        assertThat(completed.result()).isEqualTo("Task processed successfully");
    }

    @Test
    void throwsWhenTaskDoesNotExist() {
        service = new TaskService(new InMemoryTaskRepository(), new TaskProcessingProperties(1, Duration.ofSeconds(5)));

        org.junit.jupiter.api.Assertions.assertThrows(
                TaskNotFoundException.class,
                () -> service.getById(UUID.randomUUID())
        );
    }

    private TaskSnapshot waitForTerminalStatus(UUID id) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            TaskSnapshot task = service.getById(id);
            if (task.status() == TaskStatus.COMPLETED || task.status() == TaskStatus.FAILED) {
                return task;
            }
            Thread.sleep(25);
        }
        return service.getById(id);
    }
}
