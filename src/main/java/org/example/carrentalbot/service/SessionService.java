package org.example.carrentalbot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.model.enums.CarBrowsingMode;
import org.example.carrentalbot.model.enums.CarCategory;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_PREFIX = "chat:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String key(Long chatId) {
        return SESSION_PREFIX + chatId;
    }

    // --------------------------------------------------------------------
    // ✅ PUT METHODS
    // --------------------------------------------------------------------
    public void put(Long chatId, String field, Object value) {
        if (value == null) return;

        // Always store as String for consistency
        if (value instanceof UUID || value instanceof LocalDate || value instanceof Enum<?> || value instanceof BigDecimal) {
            value = value.toString();
        }

        redisTemplate.opsForHash().put(key(chatId), field, value.toString());
        redisTemplate.expire(key(chatId), DEFAULT_TTL);
    }

    // --------------------------------------------------------------------
    // ✅ GETTERS
    // --------------------------------------------------------------------

    public Optional<String> getString(Long chatId, String field) {
        return Optional.ofNullable((String) redisTemplate.opsForHash().get(key(chatId), field));
    }

    public Optional<UUID> getUUID(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(UUID.fromString(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid UUID format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    public Optional<LocalDate> getLocalDate(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(LocalDate.parse(val));
                    } catch (DateTimeParseException ex) {
                        log.error("Invalid date format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    public Optional<BigDecimal> getBigDecimal(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(new BigDecimal(val));
                    } catch (DateTimeParseException ex) {
                        log.error("Invalid big decimal format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    public Optional<CarCategory> getCarCategory(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(CarCategory.valueOf(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid CarCategory enum format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    public Optional<FlowContext> getFlowContext(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(FlowContext.valueOf(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid FlowContext enum format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    public Optional<CarBrowsingMode> getCarBrowsingMode(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(CarBrowsingMode.valueOf(val));
                    } catch (IllegalArgumentException ex) {
                        log.error("Invalid CarBrowsingMode enum format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
                        delete(chatId, field);
                        return Optional.empty();
                    }
                });
    }

    // --------------------------------------------------------------------
    // ✅ ADMIN METHODS
    // --------------------------------------------------------------------
    public Map<String, Object> getAll(Long chatId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key(chatId));
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        Map.Entry::getValue
                ));
    }

    private void delete(Long chatId, String field) {
        redisTemplate.opsForHash().delete(key(chatId), field);
    }

    public void deleteAll(Long chatId) {
        redisTemplate.delete(key(chatId));
    }

    public boolean exists(Long chatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(chatId)));
    }
}
