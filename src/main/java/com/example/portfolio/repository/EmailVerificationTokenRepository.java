package com.example.portfolio.repository;

import com.example.portfolio.domain.EmailVerificationToken;
import com.example.portfolio.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUserId(Long userId);
    void deleteByUserId(Long userId);

    void deleteByUser(User user);
}
