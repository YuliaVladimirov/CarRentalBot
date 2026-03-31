package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.carrentalbot.model.enums.ReminderStatus;
import org.example.carrentalbot.model.enums.ReminderType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a scheduled reminder notification for a booking.
 * <p>Each reminder is tied to a specific booking and defines when and
 * what type of notification should be sent to the customer.</p>
 * <p>Used to manage time-based customer notifications during the
 * rental lifecycle.</p>
 * <p>Mapped to the {@code reminders} table.</p>
 *
 * @see org.example.carrentalbot.model.Booking
 * @see org.example.carrentalbot.model.enums.ReminderType
 * @see org.example.carrentalbot.model.enums.ReminderStatus
 */
@Entity
@Table(name = "reminders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Reminder {

    /**
     * Unique identifier of the reminder.
     * Generated automatically as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    /**
     * Booking associated with this reminder.
     *
     * @see org.example.carrentalbot.model.Booking
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * Type of reminder defining its purpose and timing within the rental lifecycle.
     *
     * @see org.example.carrentalbot.model.enums.ReminderType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 30)
    private ReminderType reminderType;

    /**
     * Scheduled date and time when the reminder should be processed.
     */
    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    /**
     * Current status of the reminder.
     * <p>Only {@code PENDING} reminders are eligible for processing.</p>
     *
     * @see org.example.carrentalbot.model.enums.ReminderStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_status", nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    /**
     * Timestamp when the reminder was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
