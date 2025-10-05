package org.example.carrentalbot.service;

import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class NavigationService {

    private final Map<Long, Deque<String>> navigationHistory = new ConcurrentHashMap<>();

    /**
     * Push a new page/state onto the user's stack.
     */
    public void push(Long chatId, String state) {
        navigationHistory
                .computeIfAbsent(chatId, id -> new ConcurrentLinkedDeque<>());
        Deque<String> stack = navigationHistory.get(chatId);
        if (stack.isEmpty() || !stack.peek().equals(state)) {
            stack.push(state);
        }
    }

    /**
     * Pop the last state (go one step back).
     * Returns null if no history is available.
     */
    public String pop(Long chatId) {
        return Optional.ofNullable(navigationHistory.get(chatId))
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    stack.pop();
                    return stack.peek();
                })
                .orElse(null);
    }

    /**
     * Peek the current state without removing.
     */
    public String peek(Long chatId) {
        return Optional.ofNullable(navigationHistory.get(chatId))
                .filter(stack -> !stack.isEmpty())
                .map(Deque::peek)
                .orElse(null);
    }

    /**
     * Clear all navigation history for this user.
     */
    public void clear(Long chatId) {
        Optional.ofNullable(navigationHistory.get(chatId))
                .ifPresent(stack -> navigationHistory.remove(chatId));
    }
}
