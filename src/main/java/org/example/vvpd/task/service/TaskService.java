package org.example.vvpd.task.service;

import jakarta.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.example.vvpd.config.TaskProcessingProperties;
import org.example.vvpd.task.dto.CreateTaskRequest;
import org.example.vvpd.task.dto.PagedResponse;
import org.example.vvpd.task.exception.TaskNotFoundException;
import org.example.vvpd.task.model.Task;
import org.example.vvpd.task.model.TaskPriority;
import org.example.vvpd.task.model.TaskSnapshot;
import org.example.vvpd.task.model.TaskStatus;
import org.example.vvpd.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final TaskProcessingProperties properties;
    private final Clock clock;
    private final PriorityBlockingQueue<QueuedTask> queue = new PriorityBlockingQueue<>();
    private final AtomicLong sequence = new AtomicLong();
    private final ExecutorService workers;
    private volatile boolean running = true;

    @Autowired
    public TaskService(TaskRepository repository, TaskProcessingProperties properties) {
        this(repository, properties, Clock.systemUTC());
    }

    TaskService(TaskRepository repository, TaskProcessingProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
        this.workers = Executors.newFixedThreadPool(properties.workers(), new TaskWorkerThreadFactory());
        for (int i = 0; i < properties.workers(); i++) {
            workers.submit(this::workerLoop);
        }
    }

    public TaskSnapshot create(CreateTaskRequest request) {
        Task task = new Task(
                UUID.randomUUID(),
                request.title(),
                request.description(),
                request.priority(),
                Instant.now(clock)
        );
        repository.save(task);
        task.markPending();
        TaskSnapshot snapshot = task.snapshot();
        queue.offer(new QueuedTask(snapshot.id(), snapshot.priority(), snapshot.createdAt(), sequence.incrementAndGet()));
        return snapshot;
    }

    public PagedResponse<TaskSnapshot> findAll(TaskStatus status, TaskPriority priority, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(Math.min(size, 100), 1);
        List<TaskSnapshot> filtered = repository.findAllSnapshots().stream()
                .filter(task -> status == null || task.status() == status)
                .filter(task -> priority == null || task.priority() == priority)
                .sorted(Comparator.comparing(TaskSnapshot::createdAt).reversed())
                .toList();

        int from = Math.min(normalizedPage * normalizedSize, filtered.size());
        int to = Math.min(from + normalizedSize, filtered.size());
        int totalPages = filtered.isEmpty()
                ? 0
                : (int) Math.ceil((double) filtered.size() / normalizedSize);

        return new PagedResponse<>(
                filtered.subList(from, to),
                normalizedPage,
                normalizedSize,
                filtered.size(),
                totalPages
        );
    }

    public TaskSnapshot getById(UUID id) {
        return findTask(id).snapshot();
    }

    public TaskStatus getStatus(UUID id) {
        return getById(id).status();
    }

    public TaskSnapshot cancel(UUID id) {
        Task task = findTask(id);
        task.cancel(Instant.now(clock));
        return task.snapshot();
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        workers.shutdownNow();
    }

    private Task findTask(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void workerLoop() {
        while (running) {
            try {
                QueuedTask queuedTask = queue.take();
                process(queuedTask);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void process(QueuedTask queuedTask) throws InterruptedException {
        Task task = repository.findById(queuedTask.taskId()).orElse(null);
        if (task == null || !task.markInProgress(Instant.now(clock))) {
            return;
        }

        try {
            Thread.sleep(properties.processingDuration().toMillis());
            String result = "Task processed successfully";
            task.complete(result, Instant.now(clock));
        } catch (InterruptedException exception) {
            task.fail("Task processing was interrupted", Instant.now(clock));
            throw exception;
        } catch (RuntimeException exception) {
            task.fail(exception.getMessage(), Instant.now(clock));
        }
    }

    private static class TaskWorkerThreadFactory implements ThreadFactory {

        private final AtomicLong counter = new AtomicLong();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "task-worker-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
