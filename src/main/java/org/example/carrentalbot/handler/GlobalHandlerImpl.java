package org.example.carrentalbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.util.FlowContextHelper;
import org.example.carrentalbot.util.HandlerRegistry;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalHandlerImpl implements GlobalHandler {

    private final TelegramClient telegramClient;
    private final FlowContextHelper flowContextHelper;
    private final HandlerRegistry handlerRegistry;

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

    @Override
    public void handleMessage(Long chatId, MessageDto message) {

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

    private void handleCommand(Long chatId, FromDto from, String text) {
        CommandHandler handler = handlerRegistry.getCommandHandlers()
                .getOrDefault(text.toLowerCase(), handlerRegistry.getFallbackCommandHandler());

        log.info("Executing command '{}'", text.toLowerCase());

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, from);
    }

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

    @Override
    public void handleCallbackQuery(Long chatId, CallbackQueryDto callbackQuery) {

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