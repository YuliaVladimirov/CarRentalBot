package org.example.carrentalbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.HandlerRegistry;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Concrete implementation of the {@link GlobalHandler} interface.
 * <p>This service acts as the primary dispatcher for all incoming Telegram updates.
 * It is responsible for:
 * <ul>
 * <li>Securing and extracting the {@code chatId} from various update types.</li>
 * <li>Routing messages to command handlers (if starting with '/') or text handlers.</li>
 * <li>Routing callback queries to appropriate callback handlers based on the data prefix.</li>
 * <li>Managing and validating the user's current flow context before execution.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalHandlerImpl implements GlobalHandler {

    /**
     * Component used for interacting with the Telegram Bot API, specifically for actions
     * like answering callback queries.
     */
    private final TelegramClient telegramClient;

    /**
     * Helper component responsible for checking and managing the current state/context
     * of a user's interaction flow. Used to ensure the user is in an allowed context
     * before executing a handler.
     */
    private final FlowContextHelper flowContextHelper;

    /**
     * Registry holding all specialized handlers (commands, text, callbacks) and their
     * respective fallback implementations.
     */
    private final HandlerRegistry handlerRegistry;

    /**
     * Helper method to safely extract the unique chat ID from either a message
     * or a callback query update.
     * @param update The incoming Telegram update DTO.
     * @return The chat ID as a {@link Long}, or {@code null} if the chat ID could
     * not be determined.
     */
    private Long extractChatId(UpdateDto update) {
        if (update.getMessage() != null) {
            return update.getMessage().getChat() != null && update.getMessage().getChat().getId() != null
                    ? update.getMessage().getChat().getId() : null;
        }
        if (update.getCallbackQuery() != null && update.getCallbackQuery().getMessage() != null) {
            return update.getCallbackQuery().getMessage().getChat() != null && update.getCallbackQuery().getMessage().getChat().getId() != null
                    ? update.getCallbackQuery().getMessage().getChat().getId() : null;
        }
        return null;
    }

    /**
     * The main entry point for processing all updates from the Telegram webhook.
     * <p>This method is executed asynchronously to avoid blocking the incoming webhook
     * thread and ensure prompt response to the Telegram server.</p>
     * <ol>
     * <li>Extracts the {@code chatId}.</li>
     * <li>Routes the update to {@link #handleMessage(Long, MessageDto)} or
     * {@link #handleCallbackQuery(Long, CallbackQueryDto)} based on the update type.</li>
     * <li>Logs a warning for any unhandled update types.</li>
     * </ol>
     * @param update The data transfer object containing the full update details
     * from Telegram.
     */
    @Override
    @Async("telegramExecutor")
    public void handleUpdate(UpdateDto update) {

        if (update == null) {
            log.warn("Received null update");
            return;
        }

        Long chatId = extractChatId(update);

        if (update.getMessage() != null) {
            handleMessage(chatId, update.getMessage());
        } else if (update.getCallbackQuery() != null) {
            handleCallbackQuery(chatId, update.getCallbackQuery());
        } else {
            log.warn("Unhandled update: {}", update);
        }
    }

    /**
     * Processes an incoming message (text or command).
     * <ol>
     * <li>Performs basic checks for user presence and empty text.</li>
     * <li>If the text starts with '/', it is routed to {@link #handleCommand(Long, FromDto, String)}.</li>
     * <li>Otherwise, it is routed to the flexible {@link #handleText(Long, String)} handlers.</li>
     * </ol>
     * @param chatId The ID of the chat where the message originated.
     * @param message The message DTO.
     */
    private void handleMessage(Long chatId, MessageDto message) {

        FromDto from = message.getFrom();
        if (message.getFrom() == null) {
            log.warn("Missing telegram user in message");
            return;
        }

        String text = Optional.ofNullable(message.getText()).map(String::trim).orElse("");
        if (text.isBlank()) {
            log.debug("Ignoring message with empty text");
            return;
        }

        if (text.startsWith("/")) {
            handleCommand(chatId, from, text);
        } else {
            handleText(chatId, text);
        }
    }


    /**
     * Dispatches the update to a specific command handler.
     * <ol>
     * <li>Looks up the {@link CommandHandler} in the registry based on the command text (case-insensitive).</li>
     * <li>Uses the fallback command handler if a specific match is not found.</li>
     * <li>Validates the user's current flow context.</li>
     * <li>Executes the matched handler.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param from The user who sent the command.
     * @param text The full command text (e.g., "/start").
     */
    private void handleCommand(Long chatId, FromDto from, String text) {
        CommandHandler handler = handlerRegistry.getCommandHandlers()
                .getOrDefault(text.toLowerCase(), handlerRegistry.getFallbackCommandHandler());

        log.info("Executing command '{}'", text.toLowerCase());

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, from);
    }

    /**
     * Dispatches the update to the first capable text handler.
     * <ol>
     * <li>Streams through the registered text handlers.</li>
     * <li>The first {@link TextHandler} that returns {@code true} from {@code canHandle(text)} is executed.</li>
     * <li>If no specific handler can process the text, the fallback text handler is executed.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param text The text content of the message.
     */
    private void handleText(Long chatId, String text) {
        handlerRegistry.getTextHandlers().stream()
                .filter(handler -> handler.canHandle(text))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            log.info("Executing text '{}'", text);

                            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                            handler.handle(chatId, text);
                        },
                        () -> {
                            FallbackTextHandler fallbackHandler = handlerRegistry.getFallbackTextHandler();

                            log.info("Executing text '{}'", text);
                            fallbackHandler.handle(chatId, text);
                        }
                );
    }

    /**
     * Processes an incoming callback query resulting from a user pressing an inline keyboard button.
     * <ol>
     * <li>Sends an {@code answerCallbackQuery} response to remove the "loading" indicator from the button.</li>
     * <li>Finds the appropriate {@link CallbackHandler} by matching the beginning of the {@code callbackData}
     * against the keys in the handler registry.</li>
     * <li>Validates the user's current flow context.</li>
     * <li>Executes the matched handler.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The callback query DTO.
     */
    private void handleCallbackQuery(Long chatId, CallbackQueryDto callbackQuery) {

        String callbackData = Optional.ofNullable(callbackQuery.getData()).map(String::trim).orElse("");
        if (callbackData.isBlank()) {
            log.warn("Empty callback data: {}", callbackQuery);
            return;
        }

        telegramClient.answerCallbackQuery(
                AnswerCallbackQueryDto.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .text("Ok")
                        .showAlert(false)
                        .build());

        CallbackHandler handler = handlerRegistry.getCallbackHandlers().entrySet().stream()
                .filter(entry -> callbackData.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(handlerRegistry.getFallbackCallbackHandler());

        log.info("Executing callback '{}' ", callbackData);

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, callbackQuery);
    }
}