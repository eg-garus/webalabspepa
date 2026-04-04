package ru.ssau.todo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.todo.dto.TaskDto;
import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;
import ru.ssau.todo.entity.User;
import ru.ssau.todo.repository.TaskRepository;
import ru.ssau.todo.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskDto create(TaskDto dto) {
        long activeCount = taskRepository.countActiveTasksByUserId(dto.getCreatedBy());
        if (activeCount >= 10) {
            throw new IllegalStateException("Пользователь не может иметь более 10 активных задач одновременно");
        }

        User user = userRepository.findById(dto.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id " + dto.getCreatedBy() + " не найден"));

        Task task = Task.builder()
                .title(dto.getTitle())
                .status(dto.getStatus())
                .createdByUser(user)
                .createdAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    @Transactional
    public TaskDto update(Long id, TaskDto updates) {
        Task existing = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Задача с id " + id + " не найдена"));

        TaskStatus newStatus = updates.getStatus() != null ? updates.getStatus() : existing.getStatus();

        boolean willBeActive = newStatus == TaskStatus.OPEN || newStatus == TaskStatus.IN_PROGRESS;
        boolean wasActive = existing.getStatus() == TaskStatus.OPEN || existing.getStatus() == TaskStatus.IN_PROGRESS;

        if (willBeActive && !wasActive) {
            long currentActive = taskRepository.countActiveTasksByUserId(existing.getCreatedByUser().getId());
            if (currentActive >= 10) {
                throw new IllegalStateException("Нельзя сделать задачу активной — уже достигнут лимит в 10 активных задач");
            }
        }

        if (updates.getTitle() != null) {
            existing.setTitle(updates.getTitle());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }

        Task updatedTask = taskRepository.save(existing);
        return convertToDto(updatedTask);
    }

    @Transactional
    public void deleteById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Задача с id " + id + " не найдена"));

        long minutes = ChronoUnit.MINUTES.between(task.getCreatedAt(), LocalDateTime.now());

        if (minutes < 5) {
            throw new IllegalStateException("Нельзя удалять задачу, созданную менее 5 минут назад");
        }

        taskRepository.deleteById(id);
    }

    public List<TaskDto> findAll(LocalDateTime from, LocalDateTime to, Long userId) {
        LocalDateTime start = from != null ? from : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime end   = to != null ? to   : LocalDateTime.of(2100, 12, 31, 23, 59);

        List<Task> tasks = taskRepository.findAllByUserIdAndDateRange(userId, start, end);

        return tasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<TaskDto> findById(Long id) {
        return taskRepository.findById(id)
                .map(this::convertToDto);
    }

    public long countActiveTasksByUserId(Long userId) {
        return taskRepository.countActiveTasksByUserId(userId);
    }

    private TaskDto convertToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus())
                .createdBy(task.getCreatedByUser().getId())
                .createdAt(task.getCreatedAt())
                .build();
    }
}