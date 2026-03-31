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
 * Represents a rental booking between a customer and a car.
 * <p>Defines the rental period, pricing information, contact details,
 * and the current lifecycle status of the reservation.</p>
 * <p>Mapped to the {@code bookings} table.</p>
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
     * Unique identifier of the booking.
     * Generated automatically as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Customer who created this booking.
     *
     * @see org.example.carrentalbot.model.Customer
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Car reserved for this booking.
     *
     * @see org.example.carrentalbot.model.Car
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    /**
     * Start date of the rental period (inclusive).
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the rental period (inclusive).
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Total number of rental days calculated from start and end dates.
     */
    @Column (name = "total_days", nullable = false)
    private Integer totalDays;


    /**
     * Total cost of the booking, calculated from the rental duration
     * and the car’s daily rate at the time of booking.
     */
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    /**
     * Customer contact phone number provided during booking.
     */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /**
     * Customer contact email provided during booking.
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * Current status of the booking.
     * <p>Only {@code CONFIRMED} bookings block car availability.</p>
     *
     * @see org.example.carrentalbot.model.enums.BookingStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    /**
     * Timestamp when the booking was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last booking update.
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
