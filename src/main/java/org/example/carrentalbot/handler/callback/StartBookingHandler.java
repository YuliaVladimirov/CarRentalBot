package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service acts as the bridge between the discovery phase and the transactional phase
 * of the application. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code StartBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Initiating the booking lifecycle and updating
 * the user's session state to {@link FlowContext#BOOKING_FLOW}.</li>
 * <li>Providing the user with introductory instructions for the data collection sequence.</li>
 * <li>Dispatching the HTML-formatted message with the initial booking keyboard
 * to move the user toward contact details entry.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code StartBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "START_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BROWSING_FLOW}, as a booking can only be
     * started once a vehicle and dates have been identified during the browsing process.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service used to persist the state transition from browsing to booking.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline keyboard for the initial booking step.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically to deliver booking instructions.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * @return The constant {@link #KEY}.
     */
    @Override
    public String getKey() {
        return KEY;
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
     * Executes the logic to transition the user into the booking flow.
     * <ol>
     * <li>Logs the start of the booking sequence for audit and tracking purposes.</li>
     * <li><b>State Transition:</b> Updates the session {@code flowContext} to {@link FlowContext#BOOKING_FLOW}.
     * This is critical for ensuring that the user is now within a reservation process.</li>
     * <li>Prepares a welcome message outlining the step-by-step nature of the booking.</li>
     * <li>Sends an HTML-formatted message with a specialized keyboard.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'start booking' flow");

        sessionService.put(chatId, "flowContext", FlowContext.BOOKING_FLOW);
        log.debug("Session updated: 'flowContext' set to {}", FlowContext.BOOKING_FLOW);

        String text = """
                <b>Starting a New Booking</b>

                Please provide contact information step by step.
                You can cancel anytime before confirming your booking.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildStartBookingKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}
