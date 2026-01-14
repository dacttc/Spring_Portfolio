package com.example.portfolio.service;

import com.example.portfolio.domain.EmailVerificationToken;
import com.example.portfolio.domain.User;
import com.example.portfolio.dto.SignupRequest;
import com.example.portfolio.repository.EmailVerificationTokenRepository;
import com.example.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    public Optional<User> findByUsername (String username) {

        return userRepository.findByUsername(username);
    }
    public Long signup(SignupRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        String encoded = passwordEncoder.encode(req.getPassword());
        User user = User.createUser(req.getUsername(), encoded, req.getEmail());

        User saved = userRepository.save(user);

// 기존 토큰 있으면 삭제(재가입/재전송 케이스 대비)
        tokenRepository.deleteByUserId(saved.getId());

// 토큰 생성/저장
        String token = UUID.randomUUID().toString().replace("-", "");
        EmailVerificationToken evt = new EmailVerificationToken(
                saved,
                token,
                LocalDateTime.now().plusMinutes(30) // 30분 유효
        );
        tokenRepository.save(evt);

// 이메일 발송
        emailService.sendVerificationEmail(saved.getEmail(), token);

        return saved.getId();

    }

    public void updateEmail(String username, String newEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자 없음"));

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        user.changeEmail(newEmail);
    }
    public void resendVerificationByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("이미 이메일 인증이 완료된 계정입니다.");
        }

        String newToken = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime newExpiresAt = LocalDateTime.now().plusMinutes(30);

        EmailVerificationToken tokenEntity = tokenRepository.findByUserId(user.getId())
                .map(existing -> {
                    existing.refresh(newToken, newExpiresAt); // ✅ 기존 토큰 갱신
                    return existing;
                })
                .orElseGet(() -> new EmailVerificationToken(user, newToken, newExpiresAt)); // ✅ 없으면 생성
        LocalDate today = LocalDate.now();

        // 🔒 날짜가 바뀌면 초기화
        if (!today.equals(tokenEntity.getSendDate())) {
            tokenEntity.resetCount(today);
        }

        // 🔒 하루 제한
        if (tokenEntity.getSendCount() >= 5) {
            throw new IllegalStateException("하루 이메일 전송 한도를 초과했습니다.");
        }

        // 토큰은 위에서 이미 refresh 되었으므로 중복 호출 제거
        // (이전 버그: 여기서 다른 토큰으로 다시 refresh 하여 이메일과 DB 불일치)

        tokenEntity.increaseCount();
        tokenRepository.save(tokenEntity);

        // ✅ 여기서 "재발송" - newToken은 위에서 생성한 토큰과 일치
        emailService.sendVerificationEmail(user.getEmail(), newToken);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("사용자 없음"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        String encoded = passwordEncoder.encode(newPassword);
        user.changePassword(encoded);
    }

}
