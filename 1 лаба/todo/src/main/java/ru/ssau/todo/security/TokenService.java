package ru.ssau.todo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenService {

    private final String secret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenService(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET не задан в application.properties или переменных окружения!");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET слишком короткий! Минимум 32 символа.");
        }
        this.secret = secret;
    }

    /**
     * Генерация токена (Access или Refresh)
     */
    public String generateToken(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));

            String signature = createSignature(encodedPayload);

            return encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации токена", e);
        }
    }

    /**
     * Проверка токена и возврат payload
     */
    public Map<String, Object> validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Неверный формат токена");
            }

            String encodedPayload = parts[0];
            String receivedSignature = parts[1];

            String expectedSignature = createSignature(encodedPayload);

            if (!expectedSignature.equals(receivedSignature)) {
                throw new IllegalArgumentException("Неверная подпись токена");
            }

            // Декодируем payload
            byte[] decoded = Base64.getUrlDecoder().decode(encodedPayload);
            return objectMapper.readValue(decoded, new TypeReference<>() {});

        } catch (Exception e) {
            throw new IllegalArgumentException("Неверный токен: " + e.getMessage());
        }
    }

    private String createSignature(String encodedPayload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(key);

        byte[] signatureBytes = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(signatureBytes);
    }
}