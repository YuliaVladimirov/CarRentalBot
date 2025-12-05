package org.example.carrentalbot.reminder;

import org.example.carrentalbot.model.Booking;
import org.example.carrentalbot.model.Reminder;

import java.util.List;

public interface ReminderService {
    List<Reminder> createReminders(Booking booking);
    void processDueReminders();
    void cancelReminders(Booking booking);
    void purgeOldReminders();
}
