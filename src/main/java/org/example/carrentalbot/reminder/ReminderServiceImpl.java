package org.example.carrentalbot.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Default implementation of {@link ReminderService}.
 * Manages reminder creation, scheduled processing, and cleanup.
 * Uses {@link ReminderRepository} for persistence operations
 * and {@link ReminderDelivery} for sending notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderDelivery reminderDelivery;

    /** Retention period (in days) for completed or canceled reminders. */
    private static final int RETENTION_DAYS = 90;
    private static final List<ReminderStatus> ELIGIBLE_FOR_SENT = List.of(
            ReminderStatus.PENDING, ReminderStatus.FAILED);

    /**
     * Creates reminders for the given booking based on its start and end dates.
     *
     * <p>Reminders are scheduled for the day before and the day of
     * the start and end of the booking.</p>
     *
     * @param booking the booking for which reminders should be created
     * @return a list of created {@link Reminder} entities
     */
    @Override
    public List<Reminder> createReminders(Booking booking) {

        LocalDateTime now = LocalDateTime.now();
        List<Reminder> reminders = new ArrayList<>();

        log.debug("Creating new reminder: reminder type={}", ReminderType.START_DAY_BEFORE);
        LocalDateTime dueAt1 = booking.getStartDate().minusDays(1).atTime(10, 0);
        if (isReminderEligible(dueAt1, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt1)
                    .reminderType(ReminderType.START_DAY_BEFORE)
                    .build());
        }

        log.debug("Creating new reminder: reminder type={}", ReminderType.START_DAY_OF);
        LocalDateTime dueAt2 = booking.getStartDate().atTime(10, 0);
        if (isReminderEligible(dueAt2, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt2)
                    .reminderType(ReminderType.START_DAY_OF)
                    .build());
        }

        log.debug("Creating new reminder: reminder type={}", ReminderType.END_DAY_BEFORE);
        LocalDateTime dueAt3 = booking.getEndDate().minusDays(1).atTime(10, 0);
        if (isReminderEligible(dueAt3, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt3)
                    .reminderType(ReminderType.END_DAY_BEFORE)
                    .build());
        }

        log.debug("Creating new reminder: reminder type={}", ReminderType.END_DAY_OF);
        LocalDateTime dueAt4 = booking.getEndDate().atTime(10, 0);
        if (isReminderEligible(dueAt4, now)) {
            reminders.add(Reminder.builder()
                    .booking(booking)
                    .dueAt(dueAt4)
                    .reminderType(ReminderType.END_DAY_OF)
                    .build());
        }

        log.debug("Saving new reminders");
        return reminderRepository.saveAll(reminders);
    }

    /**
     * Checks whether a reminder should be scheduled.
     *
     * @param dueAt the planned reminder time
     * @param now the current time
     * @return {@code true} if the reminder is in the future
     */
    private boolean isReminderEligible(LocalDateTime dueAt, LocalDateTime now) {
        return dueAt.isAfter(now);
    }

     /**
     * {@inheritDoc}
     *
     * <p>Reminders are processed in batches. Each reminder is marked as sent
     * before delivery to avoid duplicate processing.</p>
     */
    @Override
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void processDueReminders() {

        log.info("Starting scheduled job.");

        final int BATCH_SIZE = 1000;
        final Pageable PAGE = PageRequest.of(0, BATCH_SIZE);

        List<Reminder> dueReminders;
        do {
            dueReminders = reminderRepository
                    .findDueReminders(LocalDateTime.now(), ELIGIBLE_FOR_SENT, PAGE);

            if (dueReminders.isEmpty()) {
                log.info("Finished scheduled job. No reminders were due.");
                break;
            }

            for (Reminder reminder : dueReminders) {
                try {
                    int updated = reminderRepository.markAsSent(reminder.getId(), ELIGIBLE_FOR_SENT);

                    if (updated == 1) {
                        reminderDelivery.send(reminder);

                        log.info("Reminder [{}] with id {} SENT for booking {}",
                                reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId());
                    }
                } catch (Exception exception) {
                    log.error("PERMANENT FAILURE after all retries for reminder [{}] with id {} for booking {}",
                            reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId(), exception);
                    try {
                        int updated = reminderRepository.markAsFailed(reminder.getId());
                        if (updated == 1) {
                            log.warn("Marked reminder [{}] with id {} for booking {} as FAILED.",
                                    reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId());
                        }
                    } catch (Exception dbException) {
                        log.error("FATAL: Could not mark reminder [{}] with id {} for booking {} as FAILED.",
                                reminder.getReminderType(), reminder.getId(), reminder.getBooking().getId(), dbException);
                    }
                }
            }
        } while (dueReminders.size() == BATCH_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void cancelReminders(Booking booking) {
        int cancelledCount = reminderRepository.markAsCancelled(booking.getId());

        if (cancelledCount > 0) {
            log.info("Cancelled {} pending reminders for booking: {}",
                    cancelledCount, booking.getId());
        }
    }

    /**
     * {@inheritDoc}
     * Cleans up old completed or canceled reminders based on retention policy.
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?") // 3:00 AM daily
    @Transactional
    public void purgeOldReminders() {

        LocalDateTime retentionDate = LocalDateTime.now().minusDays(RETENTION_DAYS);

        log.info("Starting reminder cleanup. Deleting completed reminders due before: {}", retentionDate);
        int deletedCount = reminderRepository.deleteCompletedRemindersDueBefore(retentionDate);
        if (deletedCount > 0) {
            log.info("Successfully purged {} old reminders.", deletedCount);
        } else {
            log.info("Reminder cleanup complete. No old reminders to purge.");
        }
    }
}
