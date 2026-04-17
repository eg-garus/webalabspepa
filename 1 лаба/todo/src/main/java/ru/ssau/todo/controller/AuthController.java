package ru.ssau.todo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.todo.dto.AuthResponseDto;
import ru.ssau.todo.dto.UserRegisterDto;
import ru.ssau.todo.dto.UserDto;
import ru.ssau.todo.security.TokenService;
import ru.ssau.todo.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * Регистрация нового пользователя
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRegisterDto registerDto) {
        UserDto registered = userService.register(registerDto);
        return ResponseEntity.ok(registered);
    }

    /**
     * Логин пользователя — возвращает Access и Refresh токены
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody UserRegisterDto loginDto) {
        try {
            // Проверяем существование пользователя и правильность пароля
            UserDto user = userService.authenticate(loginDto.getUsername(), loginDto.getPassword());

            // Создаём payload для Access Token (15 минут)
            Map<String, Object> accessPayload = new HashMap<>();
            accessPayload.put("userId", user.getId());
            accessPayload.put("username", user.getUsername());
            accessPayload.put("roles", userService.getUserRoles(user.getId()));
            accessPayload.put("iat", System.currentTimeMillis() / 1000);
            accessPayload.put("exp", (System.currentTimeMillis() / 1000) + 900); // 15 минут

            // Создаём payload для Refresh Token (7 дней)
            Map<String, Object> refreshPayload = new HashMap<>();
            refreshPayload.put("userId", user.getId());
            refreshPayload.put("iat", System.currentTimeMillis() / 1000);
            refreshPayload.put("exp", (System.currentTimeMillis() / 1000) + 604800); // 7 дней

            String accessToken = tokenService.generateToken(accessPayload);
            String refreshToken = tokenService.generateToken(refreshPayload);

            AuthResponseDto response = AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Обновление Access Token с помощью Refresh Token
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Map<String, Object> payload = tokenService.validateToken(refreshToken);
            Long userId = Long.valueOf(payload.get("userId").toString());

            // Создаём новый Access Token
            Map<String, Object> accessPayload = new HashMap<>();
            accessPayload.put("userId", userId);
            accessPayload.put("iat", System.currentTimeMillis() / 1000);
            accessPayload.put("exp", (System.currentTimeMillis() / 1000) + 900); // 15 минут

            String newAccessToken = tokenService.generateToken(accessPayload);

            AuthResponseDto response = AuthResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)   // возвращаем старый refresh token
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}