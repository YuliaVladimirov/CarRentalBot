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
 * <p>This service serves as the terminal point for a user's intent to abandon their
 * current progress. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ConfirmBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</li>
 * <li>Executing the final "Hard Reset" of the user's conversational state.</li>
 * <li>Purging all transient booking data (dates, car selection, contact info) from the session.</li>
 * <li>Providing a clean transition back to the Main Menu.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmCancelBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code ConfirmCancelBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "CONFIRM_CANCEL_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Service responsible for managing user-specific session data, specifically
     * for performing the final session cleanup.
     */
    private final SessionService sessionService;

    /**
     * Factory component responsible for constructing the "To Main Menu" keyboard,
     * providing the user with a clear exit path after their session is cleared.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver the
     * final cancellation acknowledgment message.
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
     * Returns the allowed contexts for this handler.
     * @return {@link #ALLOWED_CONTEXTS}.
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Finalizes the cancellation by clearing the user session.
     * <ol>
     * <li>Logs the confirmed cancellation for session tracking.</li>
     * <li><b>Session Cleanup:</b> Invokes {@code deleteAll} to remove all attributes associated
     * with the current {@code chatId}, effectively resetting the state machine.</li>
     * <li>Sends a final acknowledgment message to the user confirming the action.</li>
     * <li>Attaches a "Main Menu" keyboard to facilitate the start of a new interaction.</li>
     * </ol>
     * @param chatId The ID of the chat to be cleared.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'confirm cancel booking' flow");

        String text = """
                <b>Your booking has been cancelled.</b>

                All entered data was discarded.
                You can start a new booking anytime from the main menu.
                """;

        sessionService.deleteAll(chatId);
        log.debug("Session cleared: chat id={}", chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}
