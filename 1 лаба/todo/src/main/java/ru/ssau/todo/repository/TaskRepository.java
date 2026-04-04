package ru.ssau.todo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.ssau.todo.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Нативный SQL с фильтрацией
    @Query(value = """
        SELECT * FROM task
        WHERE created_by = :userId
          AND created_at BETWEEN :from AND :to
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Task> findAllByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // JPQL для подсчёта активных задач
    @Query("SELECT COUNT(t) FROM Task t " +
           "WHERE t.createdByUser.id = :userId " +
           "AND t.status IN ('OPEN', 'IN_PROGRESS')")
    long countActiveTasksByUserId(@Param("userId") Long userId);
}