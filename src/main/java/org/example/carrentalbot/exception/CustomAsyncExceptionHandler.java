package org.example.carrentalbot.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.dto.UpdateDto;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import static org.example.carrentalbot.aop.MDCFields.CHAT_ID;

/**
 * Global handler for uncaught exceptions in asynchronous execution.
 * <p>Captures unexpected failures from async Telegram bot flows and ensures the user
 * receives a safe fallback message instead of silent failure.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    /**
     * Factory for building navigation keyboards.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Client for sending messages via the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * Handles uncaught exceptions thrown during asynchronous execution.
     * <p>Attempts to resolve the chat ID from MDC or fallback {@link UpdateDto},
     * logs the error, and notifies the user with a safe fallback message.</p>
     *
     * @param exception the thrown exception
     * @param method method where the exception occurred
     * @param params method arguments passed during async execution
     */
    @Override
    public void handleUncaughtException(Throwable exception, Method method, Object... params) {

        log.error("ASYNCHRONOUS FAILURE in Telegram thread: Exception={}, Message={}", exception.getClass().getSimpleName(), exception.getMessage(), exception);

        String chatId = MDC.get(CHAT_ID.name());

        if (chatId == null && params.length > 0 && params[0] instanceof UpdateDto update) {

            Long fallbackChatId = extractChatIdFromUpdate(update);
            if (fallbackChatId != null) {
                chatId = fallbackChatId.toString();
            }
        }

        if (chatId != null) {

            String text = """
                ⚠️ Something did not work as expected
                and we couldn't complete your request
                due to an unexpected issue.

                You can try again later or return to the main menu.
                """;

            sendToUserWithMainMenu(chatId, text);
        }

        MDC.clear();
    }

    /**
     * Extracts chat ID from Telegram update object.
     *
     * @param update incoming Telegram update
     * @return chat ID if available, otherwise {@code null}
     */
    private Long extractChatIdFromUpdate(UpdateDto update) {
        if (update.getMessage() != null && update.getMessage().getChat() != null) {
            return update.getMessage().getChat().getId();
        }

        if (update.getCallbackQuery() != null
                && update.getCallbackQuery().getMessage() != null
                && update.getCallbackQuery().getMessage().getChat() != null) {
            return update.getCallbackQuery().getMessage().getChat().getId();
        }
        return null;
    }

    /**
     * Sends an error message to the user with navigation back to the main menu.
     *
     * @param chatId recipient chat ID
     * @param text message content
     */
    private void sendToUserWithMainMenu(String chatId, String text) {

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyMarkup)
                .parseMode("HTML")
                .build());
    }
}
