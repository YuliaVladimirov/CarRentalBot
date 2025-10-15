package org.example.carrentalbot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class TelegramService {

    private final TelegramClient telegramClient;
    private final Map<String, CallbackHandler> callbackHandlerMap;
    private final Map<String, CommandHandler> commandHandlerMap;
    private final List<TextHandler> textHandlerList;
    private final CommandHandler fallbackCommandHandler;
    private final CallbackHandler fallbackCallbackHandler;
    private final TextHandler fallbackTextHandler;
    private final FlowContextHelper flowContextHelper;

    public TelegramService(TelegramClient telegramClient,
                           List<CallbackHandler> callbackHandlerList,
                           List<CommandHandler> commandHandlerList,
                           List<TextHandler> textHandlerList,
                           FlowContextHelper flowContextHelper) {
        this.telegramClient = telegramClient;
        this.flowContextHelper = flowContextHelper;

        CallbackHandler tempFallbackCallbackHandler = null;
        Map<String, CallbackHandler> tempCallbackHandlers = new HashMap<>();

        for (CallbackHandler callbackHandler : callbackHandlerList) {
            if ("__FALLBACK__".equals(callbackHandler.getKey())) {
                tempFallbackCallbackHandler = callbackHandler;
            } else {
                tempCallbackHandlers.put(callbackHandler.getKey(), callbackHandler);
                log.info("Registered callback handler: {}", callbackHandler.getClass().getSimpleName());
            }
        }

        if (tempFallbackCallbackHandler == null) {
            throw new IllegalStateException("No fallback callback handler defined!");
        }

        this.fallbackCallbackHandler = tempFallbackCallbackHandler;
        this.callbackHandlerMap = tempCallbackHandlers;

        CommandHandler tempFallbackCommandHandler = null;
        Map<String, CommandHandler> tempCommandHandlers = new HashMap<>();

        for (CommandHandler commandHandler : commandHandlerList) {
            if ("__FALLBACK__".equals(commandHandler.getCommand())) {
                tempFallbackCommandHandler = commandHandler;
            } else {
                tempCommandHandlers.put(commandHandler.getCommand(), commandHandler);
                log.info("Registered command handler: {}", commandHandler.getClass().getSimpleName());
            }
        }

        if (tempFallbackCommandHandler == null) {
            throw new IllegalStateException("No fallback command handler defined!");
        }

        this.fallbackCommandHandler = tempFallbackCommandHandler;
        this.commandHandlerMap = tempCommandHandlers;

        FallbackTextHandler tempFallbackTextHandler = null;
        List<TextHandler> tempTextHandlers = new ArrayList<>();

        for (TextHandler textHandler : textHandlerList) {
            if (textHandler instanceof FallbackTextHandler fallback) {
                tempFallbackTextHandler = fallback;
            } else {
                tempTextHandlers.add(textHandler);
                log.info("Registered text handler: {}", textHandler.getClass().getSimpleName());
            }
        }

        if (tempFallbackTextHandler == null) {
            throw new IllegalStateException("FallbackTextHandler not found");
        }

        this.fallbackTextHandler = tempFallbackTextHandler;
        this.textHandlerList = tempTextHandlers;
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
            return;
        }

        Long chatId = message.getChat().getId();
        String text = Optional.ofNullable(message.getText()).orElse("").trim();

        if (text.isEmpty()) {
            log.debug("Ignoring empty message from chatId {}", chatId);
            return;
        }

        if (text.startsWith("/")) {
            log.info("Executing command '{}' for chatId {}", text.toLowerCase(), chatId);

            CommandHandler handler = commandHandlerMap
                    .getOrDefault(text.toLowerCase(), fallbackCommandHandler);

            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
            handler.handle(chatId, message);
            return;
        }

        log.info("Executing message for chatId {}: {}", chatId, text);
        textHandlerList.stream()
                .filter(handler -> handler.canHandle(text))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                            handler.handle(chatId, message);
                        },
                        () -> fallbackTextHandler.handle(chatId, message)
                );
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

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, callbackQuery);
    }
}
