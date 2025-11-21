package org.example.carrentalbot.reminder;

import org.example.carrentalbot.model.Reminder;

public interface ReminderDelivery {
    void send(Reminder reminder);
}
