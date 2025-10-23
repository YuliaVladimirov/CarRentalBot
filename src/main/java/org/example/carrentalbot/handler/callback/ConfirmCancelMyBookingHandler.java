package org.example.carrentalbot.handler.callback;

import org.example.carrentalbot.dto.CallbackQueryDto;
import org.example.carrentalbot.dto.InlineKeyboardMarkupDto;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.DataNotFoundException;
import org.example.carrentalbot.model.enums.FlowContext;
import org.example.carrentalbot.service.BookingService;
import org.example.carrentalbot.service.NavigationService;
import org.example.carrentalbot.service.SessionService;
import org.example.carrentalbot.util.KeyboardFactory;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

@Component
public class ConfirmCancelMyBookingHandler implements CallbackHandler {

    private static final EnumSet<FlowContext> ALLOWED_CONTEXTS = EnumSet.of(FlowContext.MY_BOOKINGS_FLOW);
    public static final String KEY = "CONFIRM_CANCEL_MY_BOOKING";

    private final BookingService bookingService;
    private final SessionService sessionService;
    private final NavigationService navigationService;
    private final TelegramClient telegramClient;
    private final KeyboardFactory keyboardFactory;


    public ConfirmCancelMyBookingHandler(BookingService bookingService, SessionService sessionService,
                                         NavigationService navigationService,
                                         TelegramClient telegramClient,
                                         KeyboardFactory keyboardFactory) {
        this.bookingService = bookingService;
        this.sessionService = sessionService;
        this.navigationService = navigationService;
        this.telegramClient = telegramClient;
        this.keyboardFactory = keyboardFactory;
    }

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

        UUID bookingId = sessionService.get(chatId, "bookingId", UUID.class).orElseThrow(() -> new DataNotFoundException(chatId, "Booking id not found in session"));
        bookingService.cancelBooking(chatId,bookingId);

        String text = String.format("""
                    ✅ Booking successfully canceled.
                    Booking id: %s
                    
                    You can start a new booking anytime from the main menu.
                    """, bookingId);

        InlineKeyboardMarkupDto replyMarkup = keyboardFactory.buildMainMenuKeyboard();

        sessionService.clear(chatId);
        navigationService.clear(chatId);

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(replyMarkup)
                .build());
    }
}
