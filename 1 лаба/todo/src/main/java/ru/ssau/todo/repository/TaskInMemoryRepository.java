package ru.ssau.todo.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import ru.ssau.todo.MyErrorException;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

@Repository
public class TaskInMemoryRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Task create(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        Long newId = idGenerator.incrementAndGet();
        task.setId(newId);
        task.setCreatedAt(LocalDateTime.now());
        tasks.put(newId, task);
        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        LocalDateTime start = from != null ? from : LocalDateTime.MIN;
        LocalDateTime end   = to   != null ? to   : LocalDateTime.MAX;

        return tasks.values().stream()
                .filter(t -> t.getCreatedBy() == userId)
                .filter(t -> !t.getCreatedAt().isBefore(start))
                .filter(t -> !t.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());
    }

    @Override
    public void update(Task task) throws MyErrorException {
        if (task == null || task.getId() == null || !tasks.containsKey(task.getId())) {
            throw new MyErrorException("Task not found with id: " + (task != null ? task.getId() : "null"));
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteById(long id) {
        tasks.remove(id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        return tasks.values().stream()
                .filter(t -> t.getCreatedBy() == userId)
                .filter(t -> t.getStatus() == TaskStatus.OPEN || t.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
    }
}