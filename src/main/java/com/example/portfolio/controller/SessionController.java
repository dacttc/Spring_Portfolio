package com.example.portfolio.controller;

import com.example.portfolio.config.SecurityConfig;
import com.example.portfolio.service.SessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 세션 관리 API 컨트롤러
 * 기존 세션 강제 종료 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * 기존 세션 강제 종료 API
     * 로그인 시 다른 곳에서 이미 로그인된 경우, 기존 세션을 끊을 때 사용
     */
    @PostMapping("/force-logout")
    public ResponseEntity<?> forceLogoutExistingSessions(HttpSession session) {
        // 세션에서 충돌된 사용자명 조회
        String username = (String) session.getAttribute(SecurityConfig.SESSION_CONFLICT_USERNAME_KEY);

        if (username == null || username.isBlank()) {
            log.warn("세션 강제 종료 실패: 세션에 사용자명 없음");
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "세션 정보가 없습니다. 다시 로그인해주세요."));
        }

        try {
            int invalidatedCount = sessionService.invalidateUserSessions(username);
            log.info("세션 강제 종료 완료: username={}, count={}", username, invalidatedCount);

            // 세션에서 충돌 정보 제거
            session.removeAttribute(SecurityConfig.SESSION_CONFLICT_USERNAME_KEY);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "기존 세션이 종료되었습니다. 다시 로그인해주세요.",
                    "invalidatedCount", invalidatedCount
            ));
        } catch (Exception e) {
            log.error("세션 강제 종료 오류: username={}", username, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "세션 종료 중 오류가 발생했습니다."));
        }
    }
}
