package ru.ssau.todo.dto;

import lombok.*;
import ru.ssau.todo.entity.TaskStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    private Long id;
    private String title;
    private TaskStatus status;
    private Long createdBy;        // id пользователя
    private LocalDateTime createdAt;
}