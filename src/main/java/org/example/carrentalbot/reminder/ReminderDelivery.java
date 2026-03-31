package org.example.carrentalbot.reminder;

import org.example.carrentalbot.model.Reminder;


/**
 * Component responsible for sending reminders through supported channels.
 */
public interface ReminderDelivery {

    /**
     * Sends the given reminder to the user.
     *
     * @param reminder the {@link Reminder} to send
     */
    void send(Reminder reminder);
}
