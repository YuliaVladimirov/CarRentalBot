package org.example.carrentalbot.repository;

import org.example.carrentalbot.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
}
