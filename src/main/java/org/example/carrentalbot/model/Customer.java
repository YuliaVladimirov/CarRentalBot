package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a registered customer of the system.
 * <p>Stores user identity information from Telegram and maintains
 * a history of associated bookings.</p>
 * <p>Mapped to the {@code customers} table.</p>
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
     * Internal unique identifier of the customer.
     * Generated automatically as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    /**
     * Unique Telegram user identifier.
     * <p>Used as the external identifier for the customer.</p>
     */
    @Column(name = "telegram_user_id", nullable = false, unique = true)
    private Long telegramUserId;

    /**
     * Telegram chat identifier used for messaging the customer.
     */
    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    /**
     * Optional Telegram username.
     */
    @Column(name = "username", length = 100)
    private String userName;

    /**
     * Customer first name from Telegram profile.
     */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /**
     * Customer last name from Telegram profile.
     */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Timestamp when the customer was created.
     * <p>Automatically generated and not updatable.</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Bookings associated with this customer.
     * <p>Lazy-loaded one-to-many relationship. Cascade operations apply to bookings.</p>
     *
     * @see org.example.carrentalbot.model.Booking
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
