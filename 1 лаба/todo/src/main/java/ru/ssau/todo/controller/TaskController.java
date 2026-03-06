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
import ru.ssau.todo.repository.TaskRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository repository;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Task> getAll(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam Long userId) {

        LocalDateTime start = from != null ? LocalDateTime.parse(from) : LocalDateTime.MIN;
        LocalDateTime end   = to   != null ? LocalDateTime.parse(to)   : LocalDateTime.MAX;

        return repository.findAll(start, end, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        Task created = new Task(task.getTitle(), task.getStatus(), task.getCreatedBy());
        created = repository.create(created);
        return ResponseEntity.created(URI.create("/tasks/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Task updates) {
        Optional<Task> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task existing = existingOpt.get();
        if (updates.getTitle() != null) existing.setTitle(updates.getTitle());
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());

        try {
            repository.update(existing);
            return ResponseEntity.ok().build();
        } catch (MyErrorException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @GetMapping("/active/count")
    public long countActive(@RequestParam Long userId) {
        return repository.countActiveTasksByUserId(userId);
    }
}