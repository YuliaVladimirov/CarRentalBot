package org.example.carrentalbot.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Map<Long, Map<String, Object>> sessionData = new ConcurrentHashMap<>();

    /**
     * Stores a key-value pair in the user's session.
     */
    public void put(Long chatId, String key, Object value) {
        sessionData
                .computeIfAbsent(chatId, id -> new ConcurrentHashMap<>())
                .put(key, value);
    }

    /**
     * Retrieves a value safely cast to the given type, with optional default.
     */
    public <T> Optional<T> get(Long chatId, String key, Class<T> type) {
        return Optional.ofNullable(sessionData.get(chatId))
                .map(map -> map.get(key))
                .filter(type::isInstance)
                .map(type::cast);
    }

    /**
     * Removes a key from the user's session.
     */
    public void remove(Long chatId, String key) {
        Optional.ofNullable(sessionData.get(chatId))
                .ifPresent(map -> map.remove(key));
    }

    /**
     * Clears all session data for a user.
     */
    public void clear(Long chatId) {
        sessionData.remove(chatId);
    }

    /**
     * Checks if user has a specific key.
     */
    public boolean contains(Long chatId, String key) {
        return Optional.ofNullable(sessionData.get(chatId))
                .map(map -> map.containsKey(key))
                .orElse(false);
    }

}
