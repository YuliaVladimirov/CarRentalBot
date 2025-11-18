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
import java.util.Optional;
import java.util.UUID;

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

    public void put(Long chatId, String field, Object value) {
        if (chatId == null) {
            throw new IllegalArgumentException("chatId cannot be null");
        }
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        if (value == null) {
            return;
        }
        String serialized = (value instanceof String) ? (String) value : serializeValue(value);
        String redisKey = key(chatId);

        redisTemplate.opsForHash().put(redisKey, field, serialized);
        redisTemplate.expire(redisKey, DEFAULT_TTL);
    }

    private String serializeValue(Object value) {

        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }

        if (value instanceof UUID) {
            return value.toString();
        }

        if (value instanceof LocalDate) {
            return value.toString();
        }

        if (value instanceof Integer) {
            return value.toString();
        }

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }

        throw new IllegalArgumentException(
                "Unsupported value type: " + value.getClass().getName()
        );
    }

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

    public Optional<Integer> getInteger(Long chatId, String field) {
        return getString(chatId, field)
                .flatMap(val -> {
                    try {
                        return Optional.of(Integer.valueOf(val));
                    } catch (NumberFormatException ex) {
                        log.error("Invalid big decimal format in Redis for chatId {} field '{}': {}", chatId, field, val, ex);
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
                    } catch (NumberFormatException ex) {
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

    private void delete(Long chatId, String field) {
        redisTemplate.opsForHash().delete(key(chatId), field);
    }

    public void deleteAll(Long chatId) {
        redisTemplate.delete(key(chatId));
    }
}
