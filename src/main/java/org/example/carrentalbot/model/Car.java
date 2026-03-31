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
 * Represents a car available for rental in the system.
 * <p>Stores descriptive information, pricing, availability status,
 * and booking history.</p>
 * <p>Mapped to the {@code cars} table.</p>
 *
 * @see org.example.carrentalbot.model.enums.CarCategory
 * @see org.example.carrentalbot.model.enums.CarStatus
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
     * Internal unique identifier of the car.
     * Generated automatically as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    private UUID id;

    /**
     * Car manufacturer (e.g., "Toyota", "BMW").
     */
    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    /**
     * Car model (e.g., Camry, X5).
     */
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    /**
     * Car category used for pricing and filtering.

     * @see org.example.carrentalbot.model.enums.CarCategory
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CarCategory category;

    /**
     * Optional additional description of the car.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Identifier of the car image in the storage system.
     */
    @Column(name = "image_file_id", length = 200)
    private String imageFileId;

    /**
     * Daily rental price for the car.
     */
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    /**
     * Operational status of the car.
     * <p>Only cars in {@code IN_SERVICE} status are available for booking.</p>
     *
     * @see org.example.carrentalbot.model.enums.CarStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "car_status", nullable = false)
    @Builder.Default
    private CarStatus carStatus = CarStatus.IN_SERVICE;

    /**
     * Bookings associated with this car.
     * <p>Lazy-loaded one-to-many relationship. Cascade operations apply to bookings.</p>
     *
     * @see org.example.carrentalbot.model.Booking
     */
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
