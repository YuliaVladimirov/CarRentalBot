package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service facilitates the modification of contact information during the
 * active booking process. It is responsible for:
 * <ul>
 * <li>Providing the unique {@code EditBookingHandler} identifier ({@code KEY}) for callback routing.</li>
 * <li>Defining accessibility to {@link FlowContext#BOOKING_FLOW} and {@link FlowContext#EDIT_BOOKING_FLOW}.</li>
 * <li>Managing the state transition from {@link FlowContext#BOOKING_FLOW} to {@link FlowContext#EDIT_BOOKING_FLOW}.</li>
 * <li>Ensuring the user is informed about the limitations of the edit (e.g., dates cannot be changed here).</li>
 * <li>Providing a specialized keyboard that allows users to jump between Phone and Email entry.</li>
 * <li>Routing the user back to the summary screen ({@link DisplayBookingDetailsHandler}) once edits are complete.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditBookingHandler implements CallbackHandler {

    /**
     * The unique callback data prefix used to identify {@code EditBookingHandler} and properly route callbacks.
     */
    public static final String KEY = "EDIT_BOOKING";

    /**
     * The set of application states in which this handler is permitted to execute.
     * <p>Permitted in {@code BOOKING_FLOW} (initial entry), {@code EDIT_BOOKING_FLOW} (recursive entry),
     * and {@code MY_BOOKINGS_FLOW} (cross-flow utility).</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BOOKING_FLOW, FlowContext.EDIT_BOOKING_FLOW, FlowContext.MY_BOOKINGS_FLOW);

    /**
     * Service responsible for persisting the state transition to {@link FlowContext#EDIT_BOOKING_FLOW}.
     */
    private final SessionService sessionService;

    /**
     * Factory responsible for building the edit actions keyboard.
     */
    private final KeyboardFactory keyboardFactory;

    /**
     * Component responsible for interacting with the Telegram Bot API to deliver the
     * edit instructions and menu.
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
     * Orchestrates the transition to the in-flow editing state.
     * <ol>
     * <li>Retrieves the current {@link FlowContext} to determine if a state shift is required.</li>
     * <li>Dispatches an HTML-formatted message with an editing keyboard.</li>
     * </ol>
     * @param chatId The ID of the chat.
     * @param callbackQuery The incoming callback query DTO.
     * @throws DataNotFoundException if the flow context is missing from the session.
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'edit booking' flow");

        FlowContext flowContext = sessionService
                .getFlowContext(chatId, "flowContext")
                .orElseThrow(() -> new DataNotFoundException("Flow context not found in session"));
        log.debug("Loaded from session: flowContext={}", flowContext);

        if (flowContext == FlowContext.BOOKING_FLOW) {
            sessionService.put(chatId, "flowContext", FlowContext.EDIT_BOOKING_FLOW);
            log.debug("Session updated: 'flowContext' set to {}", FlowContext.EDIT_BOOKING_FLOW);
        }

        String text = """
                ⚠️ <i>To change rental dates,</i>
                <i>please create a new booking.</i>
                
                <b>Edit Contact Info:</b>
                
                Update your phone or email below,
                then press <b>Continue</b> when done.
                """;

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildEditBookingKeyboard(DisplayBookingDetailsHandler.KEY);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}
