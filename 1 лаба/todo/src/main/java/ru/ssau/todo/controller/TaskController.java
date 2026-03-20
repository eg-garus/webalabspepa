package ru.ssau.todo.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ru.ssau.todo.MyErrorException;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam Long userId) {

        LocalDateTime start = from != null ? LocalDateTime.parse(from) : LocalDateTime.MIN;
        LocalDateTime end   = to   != null ? LocalDateTime.parse(to)   : LocalDateTime.MAX;

        List<Task> tasks = service.findAll(start, end, userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task toCreate = new Task(task.getTitle(), task.getStatus(), task.getCreatedBy());

        Task created = service.create(toCreate);

        URI location = URI.create("/tasks/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTask(
            @PathVariable Long id,
            @RequestBody Task updates) throws MyErrorException {

        Optional<Task> existingOpt = service.findById(id);

        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task existing = existingOpt.get();

        if (updates.getTitle() != null) {
            existing.setTitle(updates.getTitle());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }

        service.update(existing);

        return ResponseEntity.ok().build();
}

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        service.deleteById(id);
    }

    @GetMapping("/active/count")
    public ResponseEntity<Long> countActiveTasks(@RequestParam Long userId) {
        long count = service.countActiveTasksByUserId(userId);
        return ResponseEntity.ok(count);
    }
}