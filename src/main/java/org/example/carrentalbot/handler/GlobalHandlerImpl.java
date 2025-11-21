package org.example.carrentalbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.*;
import org.example.carrentalbot.exception.TelegramExceptionHandler;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
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
    private final TelegramExceptionHandler telegramExceptionHandler;

    @Override
    @Async("telegramExecutor")
    public void handleUpdate(UpdateDto update) {
        if (update == null) {
            log.warn("[update] Received null update");
            return;
        }
        try{
            if (update.getMessage() != null) {
                handleMessage(update.getMessage());
            } else if (update.getCallbackQuery() != null) {
                handleCallbackQuery(update.getCallbackQuery());
            } else {

                log.warn("[update] Unhandled update: {}", update);
            }
        } catch (Exception exception) {
            telegramExceptionHandler.handleException(exception, update);
        }
    }

    @Override
    public void handleMessage(MessageDto message) {
        if (message.getChat().getId() == null) {
            log.warn("[message] Missing chatId in message: {}", message);
            return;
        }
        Long chatId = message.getChat().getId();

        String text = Optional.ofNullable(message.getText()).orElse("").trim();
        if (text.isEmpty()) {
            log.debug("[message] Ignoring empty message from chatId: {}", chatId);
            return;
        }

        if (text.startsWith("/")) {
            if (message.getFrom() == null) {
                log.warn("[message] Missing telegram user in message: {}", message);
                return;
            }
            FromDto from = message.getFrom();

            handleCommand(chatId, from, text);
        } else {
            handleText(chatId, text);
        }
    }

    private void handleCommand(Long chatId, FromDto from, String text){
        CommandHandler handler = handlerRegistry.getCommandHandlers()
                .getOrDefault(text.toLowerCase(), handlerRegistry.getFallbackCommandHandler());

        log.info("[command] Executing command '{}' for chatId: {} - handler: {}",
                text.toLowerCase(), chatId, handler.getClass().getSimpleName());

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, from);
    }

    private void handleText(Long chatId, String text) {
        handlerRegistry.getTextHandlers().stream()
                .filter(handler -> handler.canHandle(text))
                .findFirst()
                .ifPresentOrElse(
                        handler -> {
                            log.info("[text] Executing text '{}' for chatId: {} - handler: {}",
                                    text, chatId, handler.getClass().getSimpleName());

                            flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
                            handler.handle(chatId, text);
                        },
                        () -> {
                            log.info("[text] Executing text '{}' for chatId: {} - handler: FallbackTextHandler",
                                    text, chatId);
                            handlerRegistry.getFallbackTextHandler().handle(chatId, text);
                        }
                );
    }

    @Override
    public void handleCallbackQuery(CallbackQueryDto callbackQuery) {
        if (callbackQuery.getMessage() == null) {
            log.warn("[callback] Missing message in callback query: {}", callbackQuery);
            return;
        }

        if (callbackQuery.getMessage().getChat() == null || callbackQuery.getMessage().getChat().getId() == null) {
            log.warn("[message] Missing chat or chatId in callback query: {}", callbackQuery);
            return;
        }
        Long chatId = callbackQuery.getMessage().getChat().getId();


        String callbackData = Optional.ofNullable(callbackQuery.getData()).orElse("").trim();
        if (callbackData.isEmpty()) {
            log.warn("[callback] Empty callback data: {}", callbackQuery);
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

        log.info("[callback] Executing callback '{}' for chatId: {} - handler: {}",
                callbackData, chatId, handler.getClass().getSimpleName());

        flowContextHelper.validateFlowContext(chatId, handler.getAllowedContexts());
        handler.handle(chatId, callbackQuery);
    }
}
