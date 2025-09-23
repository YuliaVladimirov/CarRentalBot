package org.example.carrentalbot.repository;

import org.example.carrentalbot.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}
