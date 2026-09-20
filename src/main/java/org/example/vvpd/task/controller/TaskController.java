package org.example.vvpd.task.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.example.vvpd.task.dto.CreateTaskRequest;
import org.example.vvpd.task.dto.PagedResponse;
import org.example.vvpd.task.dto.TaskResponse;
import org.example.vvpd.task.dto.TaskStatusResponse;
import org.example.vvpd.task.model.TaskPriority;
import org.example.vvpd.task.model.TaskSnapshot;
import org.example.vvpd.task.model.TaskStatus;
import org.example.vvpd.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskSnapshot task = taskService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(task.id())
                .toUri();
        return ResponseEntity.created(location).body(TaskResponse.from(task));
    }

    @GetMapping
    public PagedResponse<TaskResponse> findAll(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PagedResponse<TaskSnapshot> tasks = taskService.findAll(status, priority, page, size);
        return new PagedResponse<>(
                tasks.items().stream().map(TaskResponse::from).toList(),
                tasks.page(),
                tasks.size(),
                tasks.total(),
                tasks.totalPages()
        );
    }

    @GetMapping("/{taskId}")
    public TaskResponse getById(@PathVariable UUID taskId) {
        return TaskResponse.from(taskService.getById(taskId));
    }

    @DeleteMapping("/{taskId}")
    public TaskResponse cancel(@PathVariable UUID taskId) {
        return TaskResponse.from(taskService.cancel(taskId));
    }

    @GetMapping("/{taskId}/status")
    public TaskStatusResponse getStatus(@PathVariable UUID taskId) {
        return new TaskStatusResponse(taskId, taskService.getStatus(taskId));
    }
}
