package ru.ssau.todo.entity;

import java.time.LocalDateTime;

public class Task {
    private Long id;
    private String title;
    private TaskStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;

    public Task() {
    }

    public Task(String title, TaskStatus status, Long createdBy) {
        this.title = title;
        this.status = status;
        this.createdBy = createdBy;
    }

    // Все геттеры и сеттеры ОБЯЗАТЕЛЬНЫ!
    public Long getId()         { return id; }
    public void setId(Long id)  { this.id = id; }

    public String getTitle()            { return title; }
    public void setTitle(String title)  { this.title = title; }

    public TaskStatus getStatus()               { return status; }
    public void setStatus(TaskStatus status)    { this.status = status; }

    public Long getCreatedBy()              { return createdBy; }
    public void setCreatedBy(Long createdBy){ this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
}