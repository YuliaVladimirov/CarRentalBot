package org.example.carrentalbot.record;

import org.example.carrentalbot.model.Customer;

public record CustomerRegistration(Customer customer, boolean isNew) {}
