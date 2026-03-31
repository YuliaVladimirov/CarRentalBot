package org.example.carrentalbot.reminder;

import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;

import java.util.List;

/**
 * Service interface for managing booking reminders.
 * Handles creation, scheduling, and cleanup of reminder data.
 */
public interface ReminderService {

    /**
     * Creates reminders for the given booking.
     *
     * @param booking the booking for which reminders should be created
     * @return a list of created {@link Reminder} entities
     */
    List<Reminder> createReminders(Booking booking);

    /**
     * Processes reminders that are due and triggers their delivery.
     */
    void processDueReminders();

    /**
     * Cancels all pending reminders for the given booking.
     *
     * @param booking the booking whose reminders should be canceled
     */
    void cancelReminders(Booking booking);

    /**
     * Removes outdated reminders from the system.
     */
    void purgeOldReminders();
}
