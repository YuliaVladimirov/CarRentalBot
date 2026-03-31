package org.example.carrentalbot.repository;

import org.example.carrentalbot.model.Reminder;
import org.example.carrentalbot.model.enums.ReminderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing {@link Reminder} persistence operations.
 */
public interface ReminderRepository extends JpaRepository<Reminder, Long> {


    /**
     * Retrieves reminders that are due for processing.
     * <p>A reminder is considered due if:
     * <ul>
     *   <li>its status is within the eligible statuses</li>
     *   <li>its due time is in the past or present</li>
     * </ul>
     * </p>
     * <p>Results are ordered by due time (ascending) and include related
     * booking, car, and customer data.</p>
     *
     * @param now current timestamp used for comparison
     * @param eligibleStatuses statuses eligible for processing
     * @param pageable pagination information
     * @return list of {@link Reminder} entities that are due for processing
     */
    @Query(value = """
            SELECT r FROM Reminder r
            JOIN FETCH r.booking b
            JOIN FETCH b.car car
            JOIN FETCH b.customer c
            WHERE r.status IN (:eligibleStatuses)
            AND r.dueAt <= :now
            ORDER BY r.dueAt ASC
            """,
            countQuery = """
                SELECT count(r.id) FROM Reminder r
                WHERE r.status IN (:eligibleStatuses)
                AND r.dueAt <= :now
                """)
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now,
                                    @Param("eligibleStatuses") List<ReminderStatus> eligibleStatuses,
                                    Pageable pageable);

    /**
     * Marks a reminder as SENT if it is in one of the eligible statuses.
     *
     * @param id reminder identifier
     * @param eligibleStatuses statuses allowed to transition to SENT
     * @return number of updated records (0 or 1)
     */
    @Modifying
    @Query(value = """
            UPDATE Reminder r
            SET r.status = org.example.carrentalbot.model.enums.ReminderStatus.SENT
            WHERE r.id = :id
            AND r.status IN (:eligibleStatuses)
            """)
    int markAsSent(@Param("id") Long id,
                   @Param("eligibleStatuses") List<ReminderStatus> eligibleStatuses);

    /**
     * Marks a reminder as FAILED if it is currently PENDING.
     *
     * @param id reminder identifier
     * @return number of updated records (0 or 1)
     */
    @Modifying
    @Query(value = """
            UPDATE Reminder r
            SET r.status = org.example.carrentalbot.model.enums.ReminderStatus.FAILED
            WHERE r.id = :id
            AND r.status = org.example.carrentalbot.model.enums.ReminderStatus.PENDING
            """)
    int markAsFailed(@Param("id") Long id);


    /**
     * Cancels all pending reminders for the specified booking.
     *
     * @param bookingId booking identifier
     * @return number of updated reminders
     */
    @Modifying
    @Query(value = """
            UPDATE Reminder r
            SET r.status = org.example.carrentalbot.model.enums.ReminderStatus.CANCELLED
            WHERE r.booking.id = :bookingId
            AND r.status = org.example.carrentalbot.model.enums.ReminderStatus.PENDING
            """)
    int markAsCancelled(@Param("bookingId") UUID bookingId);

    /**
     * Deletes old completed reminders from the system.
     * <p>Only reminders with status SENT or CANCELLED are removed
     * if they are older than the retention threshold.</p>
     *
     * @param retentionDate cutoff date for deletion
     * @return number of deleted records
     */
    @Modifying
    @Query(value = """
            DELETE FROM Reminder r
            WHERE (r.status = org.example.carrentalbot.model.enums.ReminderStatus.SENT
             OR r.status = org.example.carrentalbot.model.enums.ReminderStatus.CANCELLED)
            AND r.dueAt < :retentionDate
            """)
    int deleteCompletedRemindersDueBefore(@Param("retentionDate") LocalDateTime retentionDate);
}
