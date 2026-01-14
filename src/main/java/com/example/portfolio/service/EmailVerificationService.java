package com.example.portfolio.service;

import com.example.portfolio.domain.EmailVerificationToken;
import com.example.portfolio.domain.User;
import com.example.portfolio.repository.EmailVerificationTokenRepository;
import com.example.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    public void resendVerificationEmail(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("이미 인증된 사용자");
        }

        // 기존 토큰 삭제 (있다면)
        tokenRepository.deleteByUser(user);

        // 새 토큰 생성 - 제대로 초기화된 생성자 사용
        String tokenString = generateToken();
        EmailVerificationToken token = new EmailVerificationToken(
                user,
                tokenString,
                java.time.LocalDateTime.now().plus(TOKEN_TTL)
        );

        tokenRepository.save(token);

        // 메일 발송
        emailService.sendVerificationEmail(
                user.getEmail(),
                tokenString
        );
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void verify(String token) {
        EmailVerificationToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 토큰"));

        if (t.isExpired()) throw new IllegalArgumentException("토큰 만료");

        // User를 직접 조회하여 영속성 컨텍스트에 포함시킴
        User user = userRepository.findById(t.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        user.setEmailVerified(true);
        userRepository.save(user);  // 명시적으로 저장

        // 토큰 삭제
        tokenRepository.delete(t);
    }
}
