package org.example.carrentalbot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.carrentalbot.handler.ReminderDeliveryHandler;
import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.ReminderStatus;
import org.example.carrentalbot.model.enums.ReminderType;
import org.example.carrentalbot.repository.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderDeliveryHandler deliveryHandler;

    private static final int RETENTION_DAYS = 90;
    private static final List<ReminderStatus> ELIGIBLE_FOR_SENT = List.of(
            ReminderStatus.PENDING,
            ReminderStatus.FAILED
    );
    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 1000;
    private static final Pageable PAGE = PageRequest.of(0, BATCH_SIZE);

    public ReminderService(ReminderRepository reminderRepository,
                           ReminderDeliveryHandler deliveryHandler) {
        this.reminderRepository = reminderRepository;
        this.deliveryHandler = deliveryHandler;
    }

    public void createReminders(Booking booking) {

        LocalDateTime now = LocalDateTime.now();
        List<Reminder> reminders = new ArrayList<>();

//        LocalDateTime dueAt1 = booking.getStartDate().minusDays(1).atTime(10, 0);
        LocalDateTime dueAt1 = booking.getStartDate().minusDays(1).atTime(19, 0);

        if (isReminderEligible(dueAt1, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt1)
                    .reminderType(ReminderType.START_DAY_BEFORE)
                    .build());
        }

//        LocalDateTime dueAt2 = booking.getStartDate().atTime(10, 0);
        LocalDateTime dueAt2 = booking.getStartDate().minusDays(1).atTime(19, 5);

        if (isReminderEligible(dueAt2, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt2)
                    .reminderType(ReminderType.START_DAY_OF)
                    .build());
        }

//        LocalDateTime dueAt3 = booking.getEndDate().minusDays(1).atTime(10, 0);
        LocalDateTime dueAt3 = booking.getStartDate().minusDays(1).atTime(19, 10);

        if (isReminderEligible(dueAt3, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt3)
                    .reminderType(ReminderType.END_DAY_BEFORE)
                    .build());
        }

//        LocalDateTime dueAt4 = booking.getEndDate().atTime(10, 0);
        LocalDateTime dueAt4 = booking.getStartDate().minusDays(1).atTime(19, 15);

        if (isReminderEligible(dueAt4, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt4)
                    .reminderType(ReminderType.END_DAY_OF)
                    .build());
        }

        reminderRepository.saveAll(reminders);
    }

    private boolean isReminderEligible(LocalDateTime dueAt, LocalDateTime now) {
        return dueAt.isAfter(now);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processDueReminders() {
        log.info("Starting scheduled job: processDueReminders.");

        List<Reminder> dueReminders;
        do {
            dueReminders = reminderRepository
                    .findDueReminders(LocalDateTime.now(), ELIGIBLE_FOR_SENT, PAGE);

            if (dueReminders.isEmpty()) {
                log.info("Finished scheduled job: processDueReminders. No reminders were due.");
                break;
            }

            for (Reminder reminder : dueReminders) {
                try {
                    int updated = reminderRepository.markAsSent(reminder.getId(), ELIGIBLE_FOR_SENT);
                    if (updated == 1) {
                        deliveryHandler.send(reminder);
                        log.info("Reminder [{}] with id {} sent for booking {}",
                                reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId());
                    }
                } catch (Exception exception) {
                    log.error("Failed to send reminder [{}] with id {} for booking {}",
                            reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId(), exception);

                    try {
                        int updated = reminderRepository.markAsFailed(reminder.getId());
                        if (updated == 1) {
                            log.warn("Marked reminder [{}] with id {} for booking {} as FAILED.",
                                    reminder.getReminderType(),reminder.getId(), reminder.getBooking().getId());
                        }
                    } catch (Exception dbException) {
                        log.error("FATAL: Could not mark reminder [{}] with id {} for booking {} as FAILED.",
                                reminder.getReminderType(),reminder.getId(), reminder.getBooking().getId(), dbException);
                    }
                }
            }
        } while (dueReminders.size() == BATCH_SIZE);
    }

//    @Scheduled(cron = "0 0 * * * *")
@Scheduled(cron = "0 * * * * *")
    @Transactional
    public void retryFailedReminders() {
        log.info("Starting scheduled job: retryFailedReminders.");
        List<Reminder> failedReminders = reminderRepository.findRemindersForRetry(MAX_RETRIES, PAGE);

        if (failedReminders.isEmpty()) {
            log.info("Finished scheduled job: retryFailedReminders. No reminders found for retry.");
            return;
        }

        for (Reminder reminder : failedReminders) {

            reminderRepository.incrementRetryCount(reminder.getId());
            int currentRetryCount = reminder.getRetryCount() + 1;

            try {
                deliveryHandler.send(reminder);

                reminderRepository.markAsSent(reminder.getId(), ELIGIBLE_FOR_SENT);
                log.info("Successfully resent and marked as SENT reminder [{}] with id {} for booking {}",
                        reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId());

            } catch (Exception exception) {
                log.warn("Failed retry attempt #{} for reminder reminder [{}] with id {} for booking {} ",
                        currentRetryCount, reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId(), exception);

                if (currentRetryCount >= MAX_RETRIES) {
                    int updated = reminderRepository.markAsPermanentlyFailed(reminder.getId());
                    if (updated == 1) {
                        log.error("Reminder [{}] with id {} for booking {} hit max retries. Marked as PERMANENTLY_FAILED.",
                                reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId());
                    }
                }
            }
        }
    }

    @Transactional
    public void cancelReminders(Booking booking) {

        int cancelledCount = reminderRepository.markAsCancelled(booking.getId());

        if (cancelledCount > 0) {
            log.info("Cancelled {} pending reminders for booking: {}",
                    cancelledCount, booking.getId());
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // 3:00 AM daily
    @Transactional
    public void purgeOldReminders() {

        LocalDateTime retentionDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        log.info("Starting reminder cleanup. Deleting completed reminders due before: {}", retentionDate);
        int deletedCount = reminderRepository.deleteCompletedRemindersDueBefore(retentionDate);
        if (deletedCount > 0) {
            log.info("Successfully purged {} old reminders.", deletedCount);
        } else {
            log.debug("Reminder cleanup complete. No old reminders to purge.");
        }
    }
}
