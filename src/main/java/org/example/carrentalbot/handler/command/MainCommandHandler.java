package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.MainMenuHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CommandHandler} interface.
 * <p>This service provides a global navigation shortcut to the bot's home screen.
 * It is responsible for:
 * <ul>
 * <li>Providing a direct mapping for the {@code /main} slash-command.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Delegating the UI rendering and state management directly to the
 * {@link MainMenuHandler}.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MainCommandHandler implements CommandHandler {

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} to ensure
     * that users can always return to the main menu.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * The underlying handler responsible for displaying the main menu
     * options and resetting the flow context.
     */
    private final MainMenuHandler mainMenuHandler;

    /**
     * {@inheritDoc}
     * @return The string {@code "/main"}.
     */
    @Override
    public String getCommand() {
        return "/main";
    }

    /**
     * {@inheritDoc}
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Executes the "Return to Main Menu" logic.
     * <p>This implementation follows the <b>Delegation Pattern</b>. It passes control to the {@link MainMenuHandler},
     * effectively duplicating the behavior of clicking a "Back to Main Menu"
     * inline button via a typed command.</p>
     * @param chatId The ID of the chat.
     * @param from Metadata about the Telegram user (unused as the
     * {@link MainMenuHandler} handles context resetting).
     */
    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing '/main' flow");

        mainMenuHandler.handle(chatId, null);
    }
}
