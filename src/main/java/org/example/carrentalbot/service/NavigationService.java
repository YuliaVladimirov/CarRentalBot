package org.example.carrentalbot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NavigationService {

    private final Map<Long, Deque<String>> navigationHistory = new ConcurrentHashMap<>();

    /**
     * Push a new page/state onto the user's stack.
     */
    public void push(Long chatId, String state) {
        navigationHistory
                .computeIfAbsent(chatId, id -> new ArrayDeque<>())
                .push(state);
    }

    /**
     * Pop the last state (go one step back).
     * Returns null if no history is available.
     */
    public String pop(Long chatId) {
        Deque<String> stack = navigationHistory.get(chatId);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        stack.pop();
        return stack.peek();
    }

    /**
     * Peek the current state without removing.
     */
    public String peek(Long chatId) {
        Deque<String> stack = navigationHistory.get(chatId);
        return (stack == null || stack.isEmpty()) ? null : stack.peek();
    }

    /**
     * Clear all navigation history for this user.
     */
    public void clear(Long chatId) {
        navigationHistory.remove(chatId);
    }
}
