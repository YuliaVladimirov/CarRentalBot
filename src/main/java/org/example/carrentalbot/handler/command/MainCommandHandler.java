package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.MainMenuHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Handles the {@code /main} command.
 * <p>Redirects the user to the main menu. This handler is available globally.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainCommandHandler implements CommandHandler {

    /**
     * Command identifier used to route commands to this handler.
     */
    public static final String COMMAND = "/main";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler is globally accessible and can be triggered from any
     * conversational state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Handler for displaying the main menu.
     */
    private final MainMenuHandler mainMenuHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommand() {
        return COMMAND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Redirects the user to the main menu.
     *
     * @param chatId chat identifier
     * @param from user metadata (unused)
     */
    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing '/main' flow");

        mainMenuHandler.handle(chatId, null);
    }
}
