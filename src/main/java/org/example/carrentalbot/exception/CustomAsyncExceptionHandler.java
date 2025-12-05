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

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final KeyboardFactory keyboardFactory;
    private final TelegramClient telegramClient;

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
