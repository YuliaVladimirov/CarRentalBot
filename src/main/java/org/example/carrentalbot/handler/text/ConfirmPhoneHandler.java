package org.example.carrentalbot.handler.text;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.exception.InvalidStateException;
import org.example.carrentalbot.handler.callback.*;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.regex.Pattern;

/**
 * Concrete implementation of the {@link TextHandler} interface.
 * <p>This service is responsible for capturing, validating, and persisting
 * user phone numbers across various workflows. It acts as a gatekeeper for
 * contact information by:
 * <ul>
 * <li>Validates input against a standard phone {@link Pattern}.</li>
 * <li>Defining global accessibility across all {@link FlowContext} states to support diverse workflows.</li>
 * <li>Updating the user's session with the validated phone string.</li>
 * <li>Dispatching a confirmation message with appropriate formatting and markup.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmPhoneHandler implements TextHandler {

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Uses {@link EnumSet#allOf(Class)} to support phone entry
     * from any point in the application.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.allOf(FlowContext.class);

    /**
     * Regular expression used to validate the phone number format.
     * <p>Matches international and local formats with optional '+' and spaces,
     * requiring between 9 and 16 total digits.</p>
     */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+?\\d[\\d\\s]{7,14}\\d");

    /**
     * Service responsible for managing user-specific session data, specifically
     * the entered {@code phone} (in {@link String} format).
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for constructing the inline contextual keyboard
     * for confirming the entered phone number.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver messages,
     * specifically for confirming the entered phone number.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     * Evaluates the input string against the {@link #PHONE_PATTERN}.
     * @param text The user's text input.
     * @return {@code true} if the text matches the phone number regex; {@code false} otherwise.
     */
    @Override
    public boolean canHandle(String text) {
        return text != null && PHONE_PATTERN.matcher(text.trim()).matches();
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
     * Processes the captured phone number.
     * <ol>
     * <li>Logs the phone confirmation attempt.</li>
     * <li>Saves the phone number to the {@link SessionService}.</li>
     * <li>Determines the next step's routing key via {@link #getDataForKeyboard}.</li>
     * <li>Sends a confirmation message with an "OK" button to proceed.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param phone The validated phone number string.
     */
    @Override
    public void handle(Long chatId, String phone) {
        log.info("Processing 'confirm phone' flow");

        sessionService.put(chatId, "phone", phone);
        log.debug("Session updated: 'phone' set to {}", phone);

        String text = String.format("""
                Confirm your phone:
                <b>%s</b>
                
                Press <b>OK</b> to continue
                or enter a new number.
                """, phone);

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
     * @throws DataNotFoundException if the context is missing.
     * @throws InvalidStateException if the context is not supported by this handler.
     */
    private String getDataForKeyboard(Long chatId) {
        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException("Flow context not found in session."));
        log.debug("Loaded from session: flowContext={}", flowContext);

        return switch (flowContext) {
            case BOOKING_FLOW -> AskForEmailHandler.KEY;
            case MY_BOOKINGS_FLOW -> EditMyBookingHandler.KEY;
            case EDIT_BOOKING_FLOW -> EditBookingHandler.KEY;
            default -> throw new InvalidStateException("Unexpected flow context for current handler: " + flowContext);
        };
    }
}
