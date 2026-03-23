package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service manages the cancellation intent during a booking or editing session.
 * Rather than performing an immediate deletion, it acts as a confirmation gatekeeper. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code ConfirmBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</li>
 * <li>Present a verification prompt to prevent accidental data loss.</li>
 * <li>Provide a specialized keyboard for final cancellation confirmation or resumption.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancelBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code CancelBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "CANCEL_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Restricted to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW);

    /**
     * Factory responsible for constructing the binary "Confirm/Deny" cancellation keyboard.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver the
     * cancellation verification message.
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
     * Executes the logic to present a cancellation confirmation dialog.
     * <ol>
     * <li>Logs the initiation of the cancellation flow.</li>
     * <li>Builds a specific "Confirm/Deny" cancellation keyboard via {@link KeyboardFactory}.</li>
     * <li>Sends a plain-text verification query to the user.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'cancel booking' flow");

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildCancelBookingKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text("Are you sure you want to cancel this booking?")
                .replyMarkup(replyMarkup)
                .build());
    }
}
