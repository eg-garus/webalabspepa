package ru.ssau.todo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.service.TaskService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    /**
     * GET /tasks?userId={id}&from={date}&to={date}
     */
    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam Long userId) {

        LocalDateTime start = (from != null)
                ? LocalDateTime.parse(from)
                : LocalDateTime.of(2000, 1, 1, 0, 0);

        LocalDateTime end = (to != null)
                ? LocalDateTime.parse(to)
                : LocalDateTime.of(2100, 12, 31, 23, 59);

        List<TaskDto> tasks = service.findAll(start, end, userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * GET /tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /tasks
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto) {
        TaskDto created = service.create(taskDto);

        URI location = URI.create("/tasks/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    /**
     * PUT /tasks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDto updates) {

        service.update(id, updates);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /tasks/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        service.deleteById(id);
    }

    /**
     * GET /tasks/active/count?userId={id}
     */
    @GetMapping("/active/count")
    public ResponseEntity<Long> countActiveTasks(@RequestParam Long userId) {
        long count = service.countActiveTasksByUserId(userId);
        return ResponseEntity.ok(count);
    }
}