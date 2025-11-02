package org.example.carrentalbot.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Getter
@Slf4j
public class HandlerRegistry {

    private final Map<String, CallbackHandler> callbackHandlers;
    private final CallbackHandler fallbackCallbackHandler;

    private final Map<String, CommandHandler> commandHandlers;
    private final CommandHandler fallbackCommandHandler;

    private final List<TextHandler> textHandlers;
    private final FallbackTextHandler fallbackTextHandler;

    public HandlerRegistry(List<CallbackHandler> callbackHandlerList,
                           List<CommandHandler> commandHandlerList,
                           List<TextHandler> textHandlerList) {
        // Callbacks
        this.callbackHandlers = callbackHandlerList.stream()
                .filter(callbackHandler -> !"__FALLBACK__".equals(callbackHandler.getKey()))
                .peek(callbackHandler -> log.info("Registered callback handler: {}", callbackHandler.getClass().getSimpleName()))
                .collect(Collectors.toMap(CallbackHandler::getKey, h -> h));

        this.fallbackCallbackHandler = callbackHandlerList.stream()
                .filter(callbackHandler -> "__FALLBACK__".equals(callbackHandler.getKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback callback handler defined!"));

        // Commands
        this.commandHandlers = commandHandlerList.stream()
                .filter(commandHandler -> !"__FALLBACK__".equals(commandHandler.getCommand()))
                .peek(commandHandler -> log.info("Registered command handler: {}", commandHandler.getClass().getSimpleName()))
                .collect(Collectors.toMap(CommandHandler::getCommand, h -> h));

        this.fallbackCommandHandler = commandHandlerList.stream()
                .filter(commandHandler -> "__FALLBACK__".equals(commandHandler.getCommand()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback command handler defined!"));

        // Text Handlers
        this.textHandlers = textHandlerList.stream()
                .filter(textHandler -> !(textHandler instanceof FallbackTextHandler))
                .peek(textHandler -> log.info("Registered text handler: {}", textHandler.getClass().getSimpleName()))
                .toList();

        this.fallbackTextHandler = textHandlerList.stream()
                .filter(textHandler -> textHandler instanceof FallbackTextHandler)
                .map(textHandler -> (FallbackTextHandler) textHandler)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fallback text handler defined!"));
    }
}
