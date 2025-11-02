package org.example.carrentalbot.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.dto.UpdateDto;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TelegramExceptionHandler {

    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    /**
     * Main entry point for all exceptions thrown during update processing.
     * Called from TelegramService.handleUpdate()
     */
    public void handleException(Exception exception, UpdateDto update) {

        Long chatId = extractChatId(update);
        if (chatId == null) {
            log.error("Unhandled exception – no chatId present in update {}:", update, exception);
            return;
        }

        if (exception instanceof DataNotFoundException dataNotFoundException) {
            handleDataNotFoundException(dataNotFoundException, chatId);
            return;
        }
        if (exception instanceof InvalidDataException invalidDataException) {
            handleInvalidDataException(invalidDataException, chatId);
            return;
        }
        if (exception instanceof InvalidStateException invalidStateException) {
            handleInvalidStateException(invalidStateException, chatId);
            return;
        }
        if (exception instanceof InvalidFlowContextException invalidFlowContextException) {
            handleInvalidFlowContextException(invalidFlowContextException, chatId);
            return;
        }

        handleGenericException(exception, chatId);
    }

    private Long extractChatId(UpdateDto update) {
        if (update == null) return null;

        if (update.getMessage() != null &&
                update.getMessage().getChat() != null)
            return update.getMessage().getChat().getId();

        if (update.getCallbackQuery() != null &&
                update.getCallbackQuery().getMessage() != null &&
                update.getCallbackQuery().getMessage().getChat() != null)
            return update.getCallbackQuery().getMessage().getChat().getId();

        return null;
    }

    public void handleDataNotFoundException(DataNotFoundException exception, Long chatId) {

        log.error("Missing data for chat {}:", chatId, exception);

        String text = """
                ⚠️ Some information could not be found.
                
                Please try again or return to the main menu.
                """;

        sendToUserWithMainMenu(chatId, text);
    }

    public void handleInvalidDataException(InvalidDataException exception, Long chatId) {

        log.error("Invalid data for chat {}:", chatId, exception);

        String text = """
                ⚠️ Something went wrong while processing your data.

                Please try again or return to the main menu.
                """;

        sendToUserWithMainMenu(chatId, text);
    }

    public void handleInvalidStateException(InvalidStateException exception, Long chatId) {

        log.error("Invalid state for chat {}:", chatId, exception);

        String text = """
                ⚠️ Something went wrong while processing your data.

                Please try again or return to the main menu.
                """;

        sendToUserWithMainMenu(chatId, text);
    }

    public void handleInvalidFlowContextException(InvalidFlowContextException exception, Long chatId) {

        log.warn("Invalid flow context for chat {}:", chatId, exception);

        String text = """
                ⚠️ This action is not available right now.

                Please continue your current flow or return to the main menu.
                """;

        sendToUserWithMainMenu(chatId, text);
    }

    public void handleGenericException(Exception exception, Long chatId) {

        log.error("Unhandled exception while processing update for chat {}:", chatId, exception);

        String text = """
                ⚠️ Something did not work as expected.

                You can try again or return to the main menu.
                """;

        sendToUserWithMainMenu(chatId, text);
    }

    private void sendToUserWithMainMenu(Long chatId, String text) {

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .parseMode("HTML")
                .build());
    }
}
