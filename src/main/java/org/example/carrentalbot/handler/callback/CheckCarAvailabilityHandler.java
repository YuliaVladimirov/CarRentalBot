package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Concrete implementation of the {@link CallbackHandler} interface.
 * <p>This service checks vehicle availability for the selected dates and
 * responds with the appropriate next action.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckCarAvailabilityHandler implements CallbackHandler {

    /**
     * Callback data prefix used to route requests to this handler.
     */
    public static final String KEY = "CHECK_AVAILABILITY";

    /**
     * Allowed flow contexts for this handler.
     * <p>This handler can only be executed within the browsing flow.</p>
     */
    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.BROWSING_FLOW);

    /**
     * Service for checking car availability and booking constraints.
     */
    private final BookingService bookingService;

    /**
     * Service for managing user session state.
     */
    private final SessionService sessionService;

    /**
     * Factory for building inline keyboard for the next action.
     */
    private final KeyboardFactory keyboardFactory;

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
     * Checks car availability for the selected dates and responds with the result.
     *
     * @param chatId chat identifier
     * @param callbackQuery callback payload
     * @throws DataNotFoundException if required session data is missing
     */
    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'check availability' flow");

        UUID carId = sessionService
                .getUUID(chatId, "carId")
                .orElseThrow(() -> new DataNotFoundException("Car id not found in session"));
        log.debug("Loaded from session: carId={}", carId);

        LocalDate startDate = sessionService
                .getLocalDate(chatId, "startDate")
                .orElseThrow(() -> new DataNotFoundException("Start date not found in session"));
        log.debug("Loaded from session: startDate={}", startDate);

        LocalDate endDate = sessionService
                .getLocalDate(chatId, "endDate")
                .orElseThrow(() -> new DataNotFoundException("End date not found in session"));
        log.debug("Loaded from session: endDate={}", endDate);

        boolean available = bookingService.isCarAvailable(carId, startDate, endDate);
        log.info("Car availability checked: available={}", available);

        String carAvailable = """
                This car is <b>available</b> for your selected dates!
                
                You can proceed to booking.
                """;

        String carUnavailable = """
                Sorry, this car is <b>not available</b> for the selected dates.
                
                Please choose different dates or another car.
                """;

        String text = available ? carAvailable : carUnavailable;

        InlineKeyboardMarkupDto replyMarkup = available ? keyboardFactory.buildCarAvailableKeyboard() : keyboardFactory.buildCarUnavailableKeyboard();

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}
