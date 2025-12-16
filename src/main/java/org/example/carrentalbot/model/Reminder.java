package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.carrentalbot.model.enums.ReminderStatus;
import org.example.carrentalbot.model.enums.ReminderType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a scheduled notification related to a specific
 * {@link org.example.carrentalbot.model.Booking}.
 * This entity tracks the type of reminder (e.g., Rental Start Reminder, Rental Return Reminder),
 * its scheduled time, and its current execution status, including retry attempts.
 * <p>Maps to the {@code reminders} table and acts as a queue item for the
 * scheduled job processing system.</p>
 * <p>Uses Lombok for boilerplate code generation.</p>
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
     * The unique internal identifier for this reminder record.
     * Uses an auto-incrementing long (IDENTITY) strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    /**
     * The associated booking transaction this reminder is related to.
     * This is a mandatory Many-to-One relationship, loaded lazily.
     * @see org.example.carrentalbot.model.Booking
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * The category or purpose of the reminder (e.g., START_DAY_BEFORE, START_DAY_OF, END_DAY_BEFORE, END_DAY_OF).
     * This determines the content and logic executed when the reminder is processed.
     * Mapped as a String (ENUM) in the database.
     * @see org.example.carrentalbot.model.enums.ReminderType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 30)
    private ReminderType reminderType;

    /**
     * The exact date and time the reminder is scheduled to be processed and sent.
     * This field is mandatory.
     */
    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    /**
     * The current processing status of the reminder (e.g., PENDING, SENT, FAILED, CANCELED).
     * Defaults to {@code PENDING}. Only PENDING reminders are eligible for processing.
     * Mapped as a String (ENUM) in the database.
     * @see org.example.carrentalbot.model.enums.ReminderStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_status", nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    /**
     * The timestamp indicating when this reminder record was first created.
     * Set automatically by {@code @CreationTimestamp} and is immutable.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
