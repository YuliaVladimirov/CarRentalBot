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
 * Handles the {@code /bookings} command.
 * <p>Redirects the user to their personal bookings view. This handler is available globally.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyBookingsCommandHandler implements CommandHandler {

    /**
     * Command identifier used to route commands to this handler.
     */
    public static final String COMMAND = "/bookings";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler is globally accessible and can be triggered from any
     * conversational state.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Handler for displaying the list of user bookings.
     */
    private final DisplayMyBookingsHandler displayMyBookingsHandler;

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
     * Redirects the user to the bookings screen by delegating to the callback-based handler.
     *
     * @param chatId chat identifier
     * @param from user metadata used for booking lookup
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
