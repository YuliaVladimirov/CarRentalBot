package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.carrentalbot.model.enums.BookingStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a confirmed or proposed rental reservation transaction
 * between a specific {@link org.example.carrentalbot.model.Customer} and a {@link org.example.carrentalbot.model.Car}.
 * This entity defines the rental period, costs, contact details, and current status of the reservation.
 *
 * <p>Maps to the {@code bookings} table in the database.</p>
 * <p>The lifecycle of this entity is governed by the {@link org.example.carrentalbot.model.enums.BookingStatus}
 * which is critical for availability checks and financial reporting.</p>
 *
 * @see org.example.carrentalbot.model.Customer
 * @see org.example.carrentalbot.model.Car
 * @see org.example.carrentalbot.model.enums.BookingStatus
 */
@Entity
@Table(name = "bookings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Booking {

    /**
     * The unique internal identifier for this booking transaction.
     * Generated automatically using the UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The customer who initiated and owns this booking.
     * This is a mandatory Many-to-One relationship, loaded lazily.
     * @see org.example.carrentalbot.model.Customer
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * The specific car reserved for this booking period.
     * This is a mandatory Many-to-One relationship, loaded lazily.
     * @see org.example.carrentalbot.model.Car
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    /**
     * The first day of the rental period (inclusive). This field is mandatory.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The last day of the rental period (inclusive). This field is mandatory.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * The total number of rental days calculated from {@code startDate} to {@code endDate}.
     * This is a calculated, mandatory field.
     */
    @Column (name = "total_days", nullable = false)
    private Integer totalDays;

    /**
     * The total financial cost of the booking. This is a calculated, mandatory field
     * based on {@code totalDays} and the car's daily rate at the time of booking.
     * Stored with 10 digits total, 2 of which are after the decimal.
     */
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    /**
     * The primary contact phone number provided by the customer at the time of booking.
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * The primary contact email address provided by the customer at the time of booking.
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * The current status of the booking (e.g., PENDING, CONFIRMED, CANCELLED).
     * Only bookings with status {@code CONFIRMED} prevent overlapping reservations.
     * @see org.example.carrentalbot.model.enums.BookingStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    /**
     * The timestamp indicating when this booking record was first created.
     * Set automatically by {@code @CreationTimestamp} and is immutable.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * The timestamp indicating the last time this booking record was updated (e.g., status change).
     * Set automatically by {@code @UpdateTimestamp}.
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
