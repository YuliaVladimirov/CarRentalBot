package org.example.carrentalbot.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
public class TelegramExceptionHandler {

    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    @ExceptionHandler(DataNotFoundException.class)
    public void handleDataNotFound(DataNotFoundException exception) {

        log.error("Missing data for chat {}: {}", exception.getChatId(), exception.getMessage());

        String text = """
                ⚠️ Something did not work as expected.

                You can try again or go back to the main menu.
                """;
        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(exception.getChatId().toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .parseMode("HTML")
                .build());
    }

    @ExceptionHandler(InvalidDataException.class)
    public void handleInvalidData(InvalidDataException exception) {

        log.error("Invalid data for chat {}: {}", exception.getChatId(), exception.getMessage());

        String text = """
                ⚠️ Something did not work as expected.

                You can try again or go back to the main menu.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(exception.getChatId().toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .parseMode("HTML")
                .build());
    }

    @ExceptionHandler(InvalidStateException.class)
    public void handleInvalidStateStateException(InvalidStateException exception) {
        log.error("Internal flow state error: {}", exception.getMessage(), exception);

        String text = """
                ⚠️ Something did not work as expected.

                You can try again or go back to the main menu.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(exception.getChatId().toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .parseMode("HTML")
                .build());
    }

    @ExceptionHandler(InvalidFlowContextException.class)
    public void handleInvalidFlowContext(InvalidFlowContextException exception) {
        log.warn("Invalid flow context for chat {}: {}", exception.getChatId(), exception.getMessage());

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(exception.getChatId().toString())
                .text("⚠️ " + exception.getMessage())
                .replyMarkup(replyMarkup)
                .build());
    }
}
