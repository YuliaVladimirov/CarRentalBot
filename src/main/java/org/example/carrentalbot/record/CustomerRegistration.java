package org.example.carrentalbot.record;

import org.example.carrentalbot.model.Customer;

/**
 * Represents the result of a customer registration process.
 * <p>Contains the customer data and indicates whether the customer
 * was newly created or already existed in the system.</p>
 * <p>Used by calling components to adjust response behavior based on
 * whether this is a first-time or returning user.</p>
 *
 * @param customer the customer associated with the registration result
 * @param isNew {@code true} if the customer was newly created,
 *              {@code false} if an existing record was found
 */
public record CustomerRegistration(Customer customer, boolean isNew) {}
