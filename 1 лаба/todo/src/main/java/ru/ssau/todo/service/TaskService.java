package ru.ssau.todo.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ru.ssau.todo.MyErrorException;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(Task task) {
        long activeCount = repository.countActiveTasksByUserId(task.getCreatedBy());
        if (activeCount >= 10) {
            throw new IllegalStateException("Пользователь не может иметь более 10 активных задач");
        }

        return repository.create(task);
    }

    public void update(Task task) throws MyErrorException {
        repository.update(task);
    }

    public void deleteById(long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(task.getCreatedAt(), now);

        if (minutes < 5) {
            throw new IllegalStateException("Нельзя удалять задачу, созданную менее 5 минут назад");
        }

        repository.deleteById(id);
    }

    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        return repository.findAll(from, to, userId);
    }

    public Optional<Task> findById(long id) {
        return repository.findById(id);
    }

    public long countActiveTasksByUserId(long userId) {
        return repository.countActiveTasksByUserId(userId);
    }
}