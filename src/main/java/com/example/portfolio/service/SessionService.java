package com.example.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 세션 관리 서비스
 * 동시 접속 제한 및 세션 강제 종료 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRegistry sessionRegistry;

    /**
     * 특정 사용자의 활성 세션이 존재하는지 확인
     */
    public boolean hasActiveSessions(String username) {
        List<Object> principals = sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {
            if (principal instanceof UserDetails userDetails) {
                if (userDetails.getUsername().equals(username)) {
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                    if (!sessions.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 특정 사용자의 모든 세션 강제 종료
     * @return 종료된 세션 수
     */
    public int invalidateUserSessions(String username) {
        List<Object> principals = sessionRegistry.getAllPrincipals();
        int invalidatedCount = 0;

        for (Object principal : principals) {
            if (principal instanceof UserDetails userDetails) {
                if (userDetails.getUsername().equals(username)) {
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                    for (SessionInformation session : sessions) {
                        session.expireNow();
                        invalidatedCount++;
                        log.info("세션 강제 종료: username={}, sessionId={}", username, session.getSessionId());
                    }
                }
            }
        }

        return invalidatedCount;
    }

    /**
     * 특정 사용자의 활성 세션 수 조회
     */
    public int getActiveSessionCount(String username) {
        List<Object> principals = sessionRegistry.getAllPrincipals();

        for (Object principal : principals) {
            if (principal instanceof UserDetails userDetails) {
                if (userDetails.getUsername().equals(username)) {
                    return sessionRegistry.getAllSessions(principal, false).size();
                }
            }
        }
        return 0;
    }
}
