package org.example.carrentalbot.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.command.CommandHandler;
import org.example.carrentalbot.handler.text.FallbackTextHandler;
import org.example.carrentalbot.handler.text.TextHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all handler implementations used in update processing.
 * <p>Aggregates and organizes {@link CallbackHandler}, {@link CommandHandler},
 * and {@link TextHandler} beans, providing efficient access patterns for each type:</p>
 * <ul>
 *   <li><b>Callback handlers</b> — indexed by key for prefix-based resolution</li>
 *   <li><b>Command handlers</b> — indexed by command for direct lookup</li>
 *   <li><b>Text handlers</b> — evaluated sequentially using {@code canHandle}</li>
 * </ul>
 * <p>Each handler category defines a mandatory fallback handler that is used
 * when no specific handler matches the input.</p>
 * <p>This component is initialized at application startup by collecting all
 * handler beans from the Spring context.</p>
 */
@Component
@Getter
@Slf4j
public class HandlerRegistry {

    public static final String FALLBACK_KEY = "__FALLBACK__";

    /**
     * Callback handlers indexed by their unique key.
     */
    private final Map<String, CallbackHandler> callbackHandlers;

    /**
     * Fallback handler used when no callback key matches.
     */
    private final CallbackHandler fallbackCallbackHandler;

    /**
     * Command handlers indexed by their command string (e.g. "/start").
     */
    private final Map<String, CommandHandler> commandHandlers;

    /**
     * Fallback handler used when an unknown command is received.
     */
    private final CommandHandler fallbackCommandHandler;

    /**
     * Ordered list of text handlers evaluated sequentially.
     */
    private final List<TextHandler> textHandlers;

    /**
     * Fallback handler used when no text handler can process the input.
     */
    private final FallbackTextHandler fallbackTextHandler;

    /**
     * Constructs the registry by indexing and categorizing handler implementations.
     * <p>Handlers are discovered via Spring injection and grouped as follows:</p>
     * <ul>
     *   <li>Callback and command handlers are indexed into maps for fast lookup</li>
     *   <li>Text handlers are stored as an ordered list for sequential evaluation</li>
     * </ul>
     * <p>Fallback handlers are identified using a reserved key ({@link HandlerRegistry#FALLBACK_KEY})
     * for callback and command handlers, and by type ({@link FallbackTextHandler})
     * for text handlers.</p>
     *
     * @param callbackHandlerList all available {@link CallbackHandler} beans
     * @param commandHandlerList  all available {@link CommandHandler} beans
     * @param textHandlerList     all available {@link TextHandler} beans
     *
     * @throws IllegalStateException if any required fallback handler is missing
     */

    public HandlerRegistry(List<CallbackHandler> callbackHandlerList,
                           List<CommandHandler> commandHandlerList,
                           List<TextHandler> textHandlerList) {

        CallbackRegistration callbackRegistration = registerCallbackHandlers(callbackHandlerList);
        this.callbackHandlers = callbackRegistration.handlers();
        this.fallbackCallbackHandler = callbackRegistration.fallback();

        CommandRegistration commandRegistration = registerCommandHandlers(commandHandlerList);
        this.commandHandlers = commandRegistration.handlers();
        this.fallbackCommandHandler = commandRegistration.fallback();

        TextRegistration textRegistration = registerTextHandlers(textHandlerList);
        this.textHandlers = textRegistration.handlers();
        this.fallbackTextHandler = textRegistration.fallback();
    }

    private CallbackRegistration registerCallbackHandlers(List<CallbackHandler> handlers) {
        Map<String, CallbackHandler> registry = new HashMap<>();
        CallbackHandler fallback = null;

        for (CallbackHandler handler : handlers) {
            String key = handler.getKey();
            String handlerName = AopUtils.getTargetClass(handler).getSimpleName();

            if (FALLBACK_KEY.equals(key)) {
                if (fallback != null) {
                    throw new IllegalStateException("Multiple fallback callback handlers defined");
                }

                fallback = handler;
                log.info("Registered FallbackCallbackHandler");
                continue;
            }

            CallbackHandler previous = registry.putIfAbsent(key, handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate callback handler key '%s' for %s and %s"
                                .formatted(
                                        key,
                                        AopUtils.getTargetClass(previous).getSimpleName(),
                                        handlerName
                                )
                );
            }

            log.info("Registered callback handler: {}", handlerName);
        }

        if (fallback == null) {
            throw new IllegalStateException("No fallback callback handler defined!");
        }

        return new CallbackRegistration(registry, fallback);
    }

    private CommandRegistration registerCommandHandlers(List<CommandHandler> handlers) {
        Map<String, CommandHandler> registry = new HashMap<>();
        CommandHandler fallback = null;

        for (CommandHandler handler : handlers) {
            String command = handler.getCommand();
            String handlerName = AopUtils.getTargetClass(handler).getSimpleName();

            if (FALLBACK_KEY.equals(command)) {
                if (fallback != null) {
                    throw new IllegalStateException("Multiple fallback command handlers defined");
                }

                fallback = handler;
                log.info("Registered FallbackCommandHandler");
                continue;
            }

            CommandHandler previous = registry.putIfAbsent(command, handler);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate command handler '%s' for %s and %s"
                                .formatted(
                                        command,
                                        AopUtils.getTargetClass(previous).getSimpleName(),
                                        handlerName
                                )
                );
            }

            log.info("Registered command handler: {}", handlerName);
        }

        if (fallback == null) {
            throw new IllegalStateException("No fallback command handler defined!");
        }

        return new CommandRegistration(registry, fallback);
    }

    private TextRegistration registerTextHandlers(List<TextHandler> handlers) {
        List<TextHandler> registry = new ArrayList<>();
        FallbackTextHandler fallback = null;

        for (TextHandler handler : handlers) {
            String handlerName = AopUtils.getTargetClass(handler).getSimpleName();

            if (handler instanceof FallbackTextHandler fallbackHandler) {
                if (fallback != null) {
                    throw new IllegalStateException("Multiple fallback text handlers defined");
                }

                fallback = fallbackHandler;
                log.info("Registered FallbackTextHandler");
                continue;
            }

            registry.add(handler);
            log.info("Registered text handler: {}", handlerName);
        }

        if (fallback == null) {
            throw new IllegalStateException("No fallback text handler defined!");
        }

        return new TextRegistration(List.copyOf(registry), fallback);
    }

    private record CallbackRegistration(
            Map<String, CallbackHandler> handlers,
            CallbackHandler fallback
    ) {
    }

    private record CommandRegistration(
            Map<String, CommandHandler> handlers,
            CommandHandler fallback
    ) {
    }

    private record TextRegistration(
            List<TextHandler> handlers,
            FallbackTextHandler fallback
    ) {
    }
}
