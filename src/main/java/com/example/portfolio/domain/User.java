package com.example.portfolio.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // user는 예약어 충돌 가능해서 users 추천
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username; // 로그인 아이디

    @Column(nullable = false)
    private String password; // 암호화된 비번
    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    private User(String username, String password, String email, Role role, boolean emailVerified) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
    }
    private int emailSendCount;
    private LocalDate emailSendDate;

    // 최근 플레이한 도시 이름
    @Column(length = 50)
    private String lastPlayedCityName;

    // 최근 플레이한 시간
    private LocalDateTime lastPlayedAt;

    public void updateLastPlayedCity(String cityName) {
        this.lastPlayedCityName = cityName;
        this.lastPlayedAt = LocalDateTime.now();
    }

    public static User createUser(String username, String encodedPassword, String email) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .role(Role.USER)
                .emailVerified(false) // 🔑 기본 false
                .build();
    }


    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}
