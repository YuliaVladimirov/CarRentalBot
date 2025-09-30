package org.example.carrentalbot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.handler.CallbackHandler;
import org.example.carrentalbot.handler.CommandHandler;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class TelegramService {

    private final TelegramClient telegramClient;
    private final Map<String, CallbackHandler> callbackHandlerMap = new HashMap<>();
    private final Map<String, CommandHandler> commandHandlerMap = new HashMap<>();
    private CommandHandler fallbackCommandHandler;
    private CallbackHandler fallbackCallbackHandler;

    public TelegramService(
            TelegramClient telegramClient,
            List<CallbackHandler> callbackHandlerList,
            List<CommandHandler> commandHandlerList
    ) {
        this.telegramClient = telegramClient;

        for (CallbackHandler callbackHandler : callbackHandlerList) {
            if ("__FALLBACK__".equals(callbackHandler.getKey())) {
                this.fallbackCallbackHandler = callbackHandler;
            } else {
                this.callbackHandlerMap.put(callbackHandler.getKey(), callbackHandler);
                log.info("Registered callback handler: {}", callbackHandler.getClass().getSimpleName());
            }
        }

        if (this.fallbackCallbackHandler == null) {
            throw new IllegalStateException("No fallback callback handler defined!");
        }

        for (CommandHandler commandHandler : commandHandlerList) {
            if ("__FALLBACK__".equals(commandHandler.getCommand())) {
                this.fallbackCommandHandler = commandHandler;
            } else {
                this.commandHandlerMap.put(commandHandler.getCommand(), commandHandler);
                log.info("Registered command handler: {}", commandHandler.getClass().getSimpleName());
            }
        }

        if (this.fallbackCommandHandler == null) {
            throw new IllegalStateException("No fallback command handler defined!");
        }
    }

    public void handleUpdate(UpdateDto update) {
        if (update == null) {
            log.warn("Received null update");
            return;
        }
        if (update.getMessage() != null) {
            handleMessage(update.getMessage());
        } else if (update.getCallbackQuery() != null) {
            handleCallbackQuery(update.getCallbackQuery());
        } else {

            log.warn("Unhandled update: {}", update);
        }
    }

    public void handleMessage(MessageDto message) {

        if (message.getChat() == null) {
            log.warn("No chat id received in message: {}", message);
        }

        Long chatId = message.getChat().getId();
        String text = message.getText();
        if (text != null && text.startsWith("/")) {
            log.info("Executing command '{}' for chatId {}", text.trim().toLowerCase(), chatId);
            commandHandlerMap
                    .getOrDefault(text.trim().toLowerCase(), fallbackCommandHandler)
                    .handle(chatId, message);
        } else {
            log.debug("Ignoring non-command message for chatId {}: {}", chatId, text);
        }
    }

    public void handleCallbackQuery(CallbackQueryDto callbackQuery) {

        if (callbackQuery.getMessage() == null || callbackQuery.getMessage().getChat() == null) {
            log.warn("Invalid callbackQuery received: {}", callbackQuery);
            return;
        }

        if (callbackQuery.getData() == null || callbackQuery.getData().isEmpty()) {
            log.warn("CallbackQuery data is null or empty: {}", callbackQuery);
            return;
        }

        String callbackData = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChat().getId();

        telegramClient.answerCallbackQuery(
                AnswerCallbackQueryDto.builder()
                        .callbackQueryId(callbackQuery.getId())
                        .text("Ok")
                        .showAlert(false)
                        .build());

        log.info("Executing callback '{}' for chatId {}", callbackData, chatId);

        CallbackHandler handler = callbackHandlerMap.entrySet().stream()
                .filter(entry -> callbackData.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(fallbackCallbackHandler);

        handler.handle(chatId, callbackQuery);
    }
}
