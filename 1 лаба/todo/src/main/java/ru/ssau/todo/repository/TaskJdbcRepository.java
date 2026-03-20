package ru.ssau.todo.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import ru.ssau.todo.entity.Task;
import ru.ssau.todo.entity.TaskStatus;

@Repository
@Profile("jdbc")
public class TaskJdbcRepository implements TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public TaskJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Task create(Task task) {
        String sql = """
            INSERT INTO task (title, status, created_by, created_at)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

        Long generatedId = jdbcTemplate.queryForObject(
            sql,
            Long.class,
            task.getTitle(),
            task.getStatus().name(),
            task.getCreatedBy(),
            Timestamp.valueOf(LocalDateTime.now())
        );

        task.setId(generatedId);
        task.setCreatedAt(LocalDateTime.now());

        return task;
    }

    @Override
    public Optional<Task> findById(long id) {
        String sql = "SELECT * FROM task WHERE id = ?";
        List<Task> tasks = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToTask(rs), id);
        return tasks.isEmpty() ? Optional.empty() : Optional.of(tasks.get(0));
    }

    @Override
    public List<Task> findAll(LocalDateTime from, LocalDateTime to, long userId) {
        LocalDateTime start = from != null ? from : LocalDateTime.MIN;
        LocalDateTime end   = to   != null ? to   : LocalDateTime.MAX;

        String sql = """
            SELECT * FROM task
            WHERE created_by = ?
              AND created_at >= ?
              AND created_at <= ?
            ORDER BY created_at
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToTask(rs),
                userId, Timestamp.valueOf(start), Timestamp.valueOf(end));
    }

    @Override
    public void update(Task task) {
        String sql = """
            UPDATE task
            SET title = ?, status = ?
            WHERE id = ?
            """;

        int updated = jdbcTemplate.update(sql,
                task.getTitle(),
                task.getStatus().name(),
                task.getId());

        if (updated == 0) {
            throw new IllegalArgumentException("Task not found with id: " + task.getId());
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM task WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long countActiveTasksByUserId(long userId) {
        String sql = """
            SELECT COUNT(*) FROM task
            WHERE created_by = ?
              AND status IN ('OPEN', 'IN_PROGRESS')
            """;
        return jdbcTemplate.queryForObject(sql, Long.class, userId);
    }

    private Task mapRowToTask(java.sql.ResultSet rs) throws java.sql.SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatedBy(rs.getLong("created_by"));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return task;
    }
}