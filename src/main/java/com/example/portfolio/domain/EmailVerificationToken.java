package com.example.portfolio.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;
    @Column(nullable = false)
    private int sendCount;   // 오늘 보낸 횟수

    @Column(nullable = false)
    private LocalDate sendDate; // YYYY-MM-DD
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    public void resetCount(LocalDate today) {
        this.sendDate = today;
        this.sendCount = 0;
    }

    public void increaseCount() {
        this.sendCount++;
    }

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public EmailVerificationToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
        this.sendDate = LocalDate.now(); // ✅ null 방지
        this.sendCount = 0;
    }
    public void refresh(String newToken, LocalDateTime newExpiresAt) {
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    public static EmailVerificationToken create(User user) {
        EmailVerificationToken t = new EmailVerificationToken();
        t.user = user;
        return t;
    }

}