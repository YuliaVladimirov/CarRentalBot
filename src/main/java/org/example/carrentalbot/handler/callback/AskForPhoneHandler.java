package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

/**
 * Callback handler responsible for requesting the user's phone number.
 * <p>Operates within booking-related flows and prompts the user to provide
 * contact information in the expected format.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AskForPhoneHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "ASK_FOR_PHONE";

    /**
     * Allowed flow contexts for this handler.
     * <p>Restricted to booking-related flows where contact information is required.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(
            FlowContext.BOOKING_FLOW,
            FlowContext.EDIT_BOOKING_FLOW,
            FlowContext.MY_BOOKINGS_FLOW
    );

    /**
     * Client for sending messages via the Telegram Bot API.
     */
    private final TelegramClient telegramClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return KEY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    /**
     * Prompts the user to enter their phone number.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'ask for phone' flow");

        String text = """
                Please enter your phone.
                
                Example: +49 123 456789
                """;

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(null)
                .build());
    }
}
