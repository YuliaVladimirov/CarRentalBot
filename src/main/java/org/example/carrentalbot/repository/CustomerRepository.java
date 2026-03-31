package org.example.carrentalbot.repository;

import org.example.carrentalbot.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing {@link Customer} persistence operations.
 */
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Finds a customer by their Telegram user ID.
     *
     * @param telegramUserId the Telegram user identifier
     * @return an {@link Optional} containing the matching customer, if found
     */
    Optional<Customer> findByTelegramUserId(Long telegramUserId);
}
