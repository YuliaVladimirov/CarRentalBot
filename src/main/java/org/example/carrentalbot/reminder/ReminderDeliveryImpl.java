package org.example.carrentalbot.reminder;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.dto.SendMessageDto;
import org.example.carrentalbot.exception.EmailException;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.email.EmailServiceImpl;
import org.example.carrentalbot.util.TelegramClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderDeliveryImpl implements ReminderDelivery {

    private final EmailServiceImpl emailService;
    private final TelegramClient telegramClient;

    @Override
    @Async
    public void send(Reminder reminder) {
        sendViaTelegram(reminder);
        sendViaEmail(reminder);
    }

    private void sendViaTelegram(Reminder reminder) {

        String text = String.format("""
                Hi %s, %s

                🆔  Booking id:  %s
                📅  Rental period:  %s - %s
                📆  Total Days: %d
                🚗  Car:  %s (%s)
                """,
                reminder.getBooking().getCustomer().getFirstName(),
                reminder.getReminderType().getMessage(),
                reminder.getBooking().getId(),
                reminder.getBooking().getStartDate(),
                reminder.getBooking().getEndDate(),
                reminder.getBooking().getTotalDays(),
                reminder.getBooking().getCar().getBrand(),
                reminder.getBooking().getCar().getModel());

        telegramClient.sendMessage(SendMessageDto.builder()
                .chatId(reminder.getBooking().getCustomer().getChatId().toString())
                .text(text)
                .parseMode("HTML")
                .build());
    }

    private void sendViaEmail(Reminder reminder) {

        try {
            emailService.sendBookingReminder(reminder);
        } catch (MessagingException exception) {
            log.error("CRITICAL: Failed to initiate email send for booking {}.", reminder.getBooking().getId(), exception);
            throw new EmailException("Failed to initiate email send.", exception);
        }
    }
}
