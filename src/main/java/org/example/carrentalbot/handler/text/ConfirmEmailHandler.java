package org.example.carrentalbot.handler.text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.handler.callback.CallbackHandler;
import org.example.carrentalbot.handler.callback.DisplayBookingDetailsHandler;
import org.example.carrentalbot.handler.callback.EditBookingHandler;
import org.example.carrentalbot.handler.callback.EditMyBookingHandler;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.regex.Pattern;

/**
 * Concrete implementation of the {@link TextHandler} interface.
 * <p>This service captures, validates, and persists user email addresses.
 * It functions as a specialized text-processor that:
 * <ul>
 * <li>Validates input against a standard email {@link Pattern}.</li>
 * <li>Operates across all {@link FlowContext} states to support diverse workflows.</li>
 * <li>Updates the user's session with the validated email string.</li>
 * <li>Dispatching a confirmation message with appropriate formatting and markup.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmEmailHandler implements TextHandler {

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Uses {@link EnumSet#allOf(Class)} to support phone entry
     * from any point in the application.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Regular expression used to validate the basic structure of an email address.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Service responsible for managing user-specific session data, specifically
     * the entered {@code email} (in {@link String} format).
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline contextual keyboard
     * for confirming the entered email.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically for confirming the entered email.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * Evaluates the input string against the {@link #EMAIL_PATTERN}.
     * @param text The user's text input.
     * @return {@code true} if the text matches the email regex; {@code false} otherwise.
     */
    @Override
    public boolean canHandle(String text) {
        return text != null && EMAIL_PATTERN.matcher(text.trim()).matches();
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
     * Processes the captured email address.
     * <ol>
     * <li>Logs the email confirmation attempt.</li>
     * <li>Saves the validated email to the {@link SessionService}.</li>
     * <li>Determines the next callback target via {@link #getDataForKeyboard}.</li>
     * <li>Sends a confirmation message with an "OK" keyboard to finalize the entry.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param email The validated email address string.
     */
    @Override
    public void handle(Long chatId, String email) {
        log.info("Processing 'confirm email' flow");

        sessionService.put(chatId, "email", email);
        log.debug("Session updated: 'email' set to {}", email);

        String text = String.format("""
                Confirm your email:
                <b>%s</b>

                Press <b>OK</b> to continue
                or enter a new email.
                """, email);

        String callbackKey = getDataForKeyboard(chatId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildOkKeyboard(callbackKey);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }

    /**
     * Logic-driven router that selects the next {@link CallbackHandler} key
     * based on the user's current {@link FlowContext}.
     * Retrieves the active {@link FlowContext} from the session.
     * @param chatId The ID of the chat.
     * @return The {@code KEY} of the next {@link CallbackHandler}.
     * @throws DataNotFoundException if the context is missing from the session.
     * @throws InvalidStateException if the context is not mapped to a next step.
     */
    private String getDataForKeyboard(Long chatId) {
        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException("Flow context not found in session."));
        log.debug("Loaded from session: flowContext={}", flowContext);

        return switch (flowContext) {
            case BOOKING_FLOW -> DisplayBookingDetailsHandler.KEY;
            case EDIT_BOOKING_FLOW -> EditBookingHandler.KEY;
            case MY_BOOKINGS_FLOW -> EditMyBookingHandler.KEY;
            default -> throw new InvalidStateException("Unexpected flow context for current handler: " + flowContext);
        };
    }
}
