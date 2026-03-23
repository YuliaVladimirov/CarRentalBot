package org.example.carrentalbot.handler.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.FromDto;
import org.example.carrentalbot.handler.callback.DisplayMyBookingsHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CommandHandler} interface.
 * <p>This service provides a global shortcut to the user's personal booking
 * management dashboard. It is responsible for:
 * <ul>
 * <li>Providing a unique identifier {@code COMMAND} for proper command routing.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states.</li>
 * <li>Synthesizing a {@link CallbackQueryDto} to bridge the gap between command-based
 * input and the underlying callback-driven UI logic.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyBookingsCommandHandler implements CommandHandler {

    /**
     * The unique routing identifier used to identify {@code MainCommandHandler} and properly route commands.
     */
    public static final String COMMAND = "/bookings";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Configured to {@link EnumSet#allOf(Class)} to ensure
     * that users can check their reservations from any operational context.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * The underlying handler responsible for fetching and displaying the
     * list of bookings associated with the user.
     */
    private final DisplayMyBookingsHandler displayMyBookingsHandler;

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
     * Executes the "My Bookings" shortcut logic.
     * <p>This implementation utilizes a <b>Synthetic Callback Pattern</b>.
     * Since the {@link DisplayMyBookingsHandler} expects a callback query,
     * this method constructs a mock {@link CallbackQueryDto} containing the
     * user's metadata and the appropriate routing key. This ensures the
     * response is identical to clicking the "My Bookings" button in the main menu.</p>
     * @param chatId The ID of the chat.
     * @param from Metadata about the Telegram user used to identify ownership
     * of the bookings.
     */
    @Override
    public void handle(Long chatId, FromDto from) {
        log.info("Processing '/bookings' flow");

        CallbackQueryDto callback = CallbackQueryDto.builder()
                .from(from)
                .data(DisplayMyBookingsHandler.KEY)
                .build();

        displayMyBookingsHandler.handle(chatId, callback);
    }
}
