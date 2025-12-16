package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a registered user (Customer) of the service.
 * This entity stores key identifying information from the Telegram platform
 * and maintains a historical record of all associated bookings.
 * <p>Maps to the {@code customers} table in the database.</p>
 * <p>Uses Lombok for boilerplate code generation.</p>
 *
 * @see org.example.carrentalbot.model.Booking
 */
@Entity
@Table(name = "customers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Customer {

    /**
     * The unique internal identifier of the customer.
     * Generated automatically using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * The unique identifier assigned to the user by the Telegram platform.
     * This acts as the external primary key for identifying the user.
     * Guaranteed to be unique and non-null.
     */
    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private Long telegramUserId;

    /**
     * The unique Telegram chat ID used for initiating messages and notifications
     * with this customer. Guaranteed to be unique and non-null.
     */
    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    /**
     * The user's optional Telegram username. Length constrained to 100 characters.
     */
    @Column(name = "username", length = 100)
    private String userName;

    /**
     * Customer's first name as provided by Telegram.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * Customer's last name as provided by Telegram.
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * The system timestamp indicating when this customer record was created.
     * Set automatically by {@code @CreationTimestamp} and cannot be updated.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * A collection of all bookings associated with this customer.
     * This is a one-to-many relationship, loaded lazily. Operations on the
     * Customer cascade to its Bookings (e.g., deletion).
     * @see org.example.carrentalbot.model.Booking
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
