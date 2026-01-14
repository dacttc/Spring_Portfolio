package com.example.portfolio.controller;

import com.example.portfolio.config.SecurityConfig;
import com.example.portfolio.repository.EmailVerificationTokenRepository;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.EmailService;
import com.example.portfolio.service.EmailVerificationService;
import com.example.portfolio.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserService userService;
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, RedirectAttributes ra) {
        try {
            emailVerificationService.verify(token);
            ra.addFlashAttribute("message", "이메일 인증 완료!");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/resend-verification")
    public String resendPage() {
        // 그냥 화면만 보여주면 됨(버튼만 있는 페이지)
        return "resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resend(HttpSession session, RedirectAttributes ra) {

        String username = (String) session.getAttribute(SecurityConfig.RESEND_USERNAME_SESSION_KEY);

        if (username == null || username.isBlank()) {
            ra.addFlashAttribute("errorMessage", "재전송할 계정을 찾을 수 없습니다. 다시 로그인 후 시도해주세요.");
            return "redirect:/login";
        }

        try {
            userService.resendVerificationByUsername(username);

            // ✅ 한 번 재전송하면 세션 값 제거(깔끔)
            session.removeAttribute(SecurityConfig.RESEND_USERNAME_SESSION_KEY);

            ra.addFlashAttribute("message", "인증 메일을 다시 보냈습니다. 메일함을 확인해주세요.");
            return "redirect:/login";

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("재전송 실패: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/resend-verification";
        }
    }

}
