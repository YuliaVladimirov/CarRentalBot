package org.example.carrentalbot.handler.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.session.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelMyBookingHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);
    public static final String KEY = "CANCEL_MY_BOOKING";

    private final BookingService bookingService;
    private final SessionService sessionService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EnumSet<FlowContext> getAllowedContexts() {
        return ALLOWED_CONTEXTS;
    }

    @Override
    public void handle(Long chatId, CallbackQueryDto callbackQuery) {
        log.info("Processing 'cancel my booking' flow");

        UUID bookingId = sessionService
                .getUUID(chatId, "bookingId")
                .orElseThrow(() -> new DataNotFoundException("Booking id not found in session"));
        log.debug("Loaded from session: bookingId={}", bookingId);

        Booking booking = bookingService.getBookingById(bookingId);
        log.info("Retrieved booking: id={}", booking.getId());

        LocalDate today = LocalDate.now();

        String text;
        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildToMainMenuKeyboard();

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            text = """
                    ⚠️ This booking has already been canceled.
                    
                    You can return to the main menu.
                    """;
        } else if (today.isEqual(booking.getStartDate()) || today.isAfter(booking.getStartDate())) {
            text = """
                    ⚠️ This booking can no longer be canceled.
                    
                    Cancellations are allowed
                    up to 1 day before the rental start date.
                    """;
        } else {
            text = "Are you sure you want to cancel this booking?";

            replyMarkup = keyboardFactory.buildCancelMyBookingKeyboard();
        }

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(replyMarkup)
                .build());
    }
}
