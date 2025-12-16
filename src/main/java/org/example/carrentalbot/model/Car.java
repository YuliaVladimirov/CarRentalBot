package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.carrentalbot.model.enums.CarStatus;
import org.example.carrentalbot.model.enums.CarCategory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a vehicle available for rental in the system.
 * This entity stores the descriptive metadata, rental rates, and
 * availability status for a car, and maintains a history of all
 * associated rental bookings.
 * <p>Maps to the {@code cars} table in the database.</p>
 * <p>Uses Lombok for boilerplate code generation.</p>
 *
 * @see org.example.carrentalbot.model.enums.CarCategory
 * @see org.example.carrentalbot.model.Booking
 */
@Entity
@Table(name = "cars")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Car {

    /**
     * The unique internal identifier for this car record.
     * Generated automatically using the UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    private UUID id;

    /**
     * The brand or manufacturer of the car (e.g., "Toyota", "BMW").
     * This field is mandatory and limited to 100 characters.
     */
    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    /**
     * The specific model name of the car (e.g., "Camry", "X5").
     * This field is mandatory and limited to 100 characters.
     */
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    /**
     * The predefined category of the car, used for rental pricing and filtering
     * (e.g., SEDAN, CONVERTIBLE, SUV). Mapped as a String to the database.
     * @see org.example.carrentalbot.model.enums.CarCategory
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CarCategory category;

    /**
     * A brief description providing additional context or features of the car.
     * Optional, max length 500 characters.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * The file ID referencing the car's primary image in the asset storage system.
     * Optional. Max length 200 characters.
     */
    @Column(name = "image_file_id", length = 200)
    private String imageFileId;

    /**
     * The rental rate charged per day for this car.
     * This field is mandatory and stored with 10 digits total, 2 of which are after the decimal.
     */
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    /**
     * The administrative status of the car, which dictates whether it can
     * be rented, regardless of booking conflicts. For example, a car
     * in {@code UNDER_REPAIR} status cannot be booked.
     * Mapped as a String (ENUM) in the database.
     * @see org.example.carrentalbot.model.enums.CarStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "car_status", nullable = false)
    @Builder.Default
    private CarStatus carStatus = CarStatus.IN_SERVICE;

    /**
     * A collection of all bookings associated with this specific car.
     * This is a one-to-many relationship, loaded lazily. Operations on the
     * Car (e.g., deletion) will cascade to its corresponding Bookings.
     * @see org.example.carrentalbot.model.Booking
     */
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
