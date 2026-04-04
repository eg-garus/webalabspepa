package ru.ssau.todo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.ssau.todo.entity.User;

/**
 * Репозиторий для работы с пользователями
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // Метод findById(Long id)
}