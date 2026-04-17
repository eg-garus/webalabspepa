package ru.ssau.todo.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.ssau.todo.dto.UserDto;
import ru.ssau.todo.dto.UserRegisterDto;
import ru.ssau.todo.entity.Role;
import ru.ssau.todo.entity.User;   // ← добавь этот репозиторий
import ru.ssau.todo.repository.RoleRepository;
import ru.ssau.todo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto register(UserRegisterDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();

        String roleName = dto.getUsername().equalsIgnoreCase("admin") ? "ROLE_ADMIN" : "ROLE_USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль " + roleName + " не найдена"));

        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        return new UserDto(savedUser.getId(), savedUser.getUsername());
    }

    public UserDto authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        return new UserDto(user.getId(), user.getUsername());
    }

    public List<String> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return user.getRoles().stream()
                .map(Role::getName)
                .toList();
    }

    public User loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));
    }
}