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

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

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


    @Modifying
    @Query(value = """
            UPDATE Reminder r
            SET r.status = org.example.carrentalbot.model.enums.ReminderStatus.SENT
            WHERE r.id = :id
            AND r.status IN (:eligibleStatuses)
            """)
    int markAsSent(@Param("id") Long id,
                   @Param("eligibleStatuses") List<ReminderStatus> eligibleStatuses);


    @Modifying
    @Query(value = """
            UPDATE Reminder r
            SET r.status = org.example.carrentalbot.model.enums.ReminderStatus.FAILED
            WHERE r.id = :id
            AND r.status = org.example.carrentalbot.model.enums.ReminderStatus.PENDING
            """)
    int markAsFailed(@Param("id") Long id);


    @Modifying
    @Query(value = """
            UPDATE Reminder r
            SET r.status = org.example.carrentalbot.model.enums.ReminderStatus.CANCELLED
            WHERE r.booking.id = :bookingId
            AND r.status = org.example.carrentalbot.model.enums.ReminderStatus.PENDING
            """)
    int markAsCancelled(@Param("bookingId") UUID bookingId);


    @Modifying
    @Query(value = """
            DELETE FROM Reminder r
            WHERE (r.status = org.example.carrentalbot.model.enums.ReminderStatus.SENT
             OR r.status = org.example.carrentalbot.model.enums.ReminderStatus.CANCELLED)
            AND r.dueAt < :retentionDate
            """)
    int deleteCompletedRemindersDueBefore(@Param("retentionDate") LocalDateTime retentionDate);
}
