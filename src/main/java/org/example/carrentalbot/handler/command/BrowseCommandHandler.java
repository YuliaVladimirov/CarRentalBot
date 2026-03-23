package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.BrowseCategoriesHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CommandHandler} interface.
 * <p>This service provides a global shortcut to the car browsing and
 * selection flow. It is responsible for:
 * <ul>
 * <li>Providing a unique identifier {@code COMMAND} for proper command routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Allowing users to bypass menus and immediately view available vehicle types.</li>
 * <li>Delegating the category display logic to the {@link BrowseCategoriesHandler}.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseCommandHandler implements CommandHandler {

    /**
     * The unique routing identifier used to identify {@code MainCommandHandler} and properly route commands.
     */
    public static final String COMMAND = "/browse";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} to ensure
     * that users can start looking for a car even if they are currently in a different
     * operational context.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * The underlying handler responsible for fetching and displaying the
     * available car categories to the user.
     */
    private final BrowseCategoriesHandler browseCategoriesHandler;

    /**
     * {@inheritDoc}
     * @return The constant {@code COMMAND}.
     */
    @Override
    public String getCommand() {
        return COMMAND;
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
     * Executes the "Browse Cars" shortcut logic.
     * <p>By delegating to the {@link BrowseCategoriesHandler}, this method
     * triggers the same category list that a user would see if they navigated
     * through the inline menu buttons, ensuring a unified UI experience.</p>
     * @param chatId The ID of the chat.
     * @param from Metadata about the Telegram user (unused here as the
     * delegated handler manages the response).
     */
    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing '/browse' flow");

        browseCategoriesHandler.handle(chatId, null);
    }
}
