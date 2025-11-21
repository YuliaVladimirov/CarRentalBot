package org.example.carrentalbot.reminder;

import org.example.carrentalbot.model.Booking;

public interface ReminderService {
    void createReminders(Booking booking);
    void processDueReminders();
    void cancelReminders(Booking booking);
    void purgeOldReminders();
}
