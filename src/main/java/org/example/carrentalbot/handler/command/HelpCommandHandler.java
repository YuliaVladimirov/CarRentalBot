package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.HelpMenuHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CommandHandler} interface.
 * <p>This service provides a global shortcut to the bot's assistance and
 * documentation menu. It is responsible for:
 * <ul>
 * <li>Mapping the {@code /help} slash-command to the help subsystem.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Providing a context-agnostic way for users to find information about
 * bot features or commands.</li>
 * <li>Delegating the rendering of the help interface to the {@link HelpMenuHandler}.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HelpCommandHandler implements CommandHandler {

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} to ensure
     * that users can request assistance even if they are in the middle of a complex
     * booking or edit flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * The underlying handler responsible for displaying the help text,
     * available commands, and any associated support keyboards.
     */
    private final HelpMenuHandler helpMenuHandler;

    /**
     * {@inheritDoc}
     * @return The string {@code "/help"}.
     */
    @Override
    public String getCommand() {
        return "/help";
    }

    /**
     * {@inheritDoc}
     * @return A set containing all possible {@link FlowContext} values.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Executes the "Request Assistance" logic.
     * <p>Following the <b>Delegation Pattern</b>, this method passes control
     * directly to the {@link HelpMenuHandler}. This ensures a consistent
     * user experience between typing {@code /help} and clicking a "Help"
     * button in an inline keyboard.</p>
     * @param chatId The ID of the chat.
     * @param from Metadata about the Telegram user (unused as the
     * {@link HelpMenuHandler} manages the response content).
     */
    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing '/help' flow");
        helpMenuHandler.handle(chatId, null);
    }
}
