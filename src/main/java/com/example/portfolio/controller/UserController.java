package com.example.portfolio.controller;

import com.example.portfolio.dto.EmailUpdateRequest;
import com.example.portfolio.dto.PasswordChangeRequest;
import com.example.portfolio.dto.SignupRequest;
import com.example.portfolio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public String signup(@Valid SignupRequest req, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        log.info("POST /signup 진입");

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    log.warn("검증 실패 field={}, message={}",
                            error.getField(),
                            error.getDefaultMessage())
            );
            return "signup";
        }
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");

        Long id = userService.signup(req);
        log.info("회원가입 성공 id={}", id);

        return "redirect:/login";
    }
    @PostMapping("/mypage/email")
    public String updateEmail(@Valid EmailUpdateRequest req, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {
        log.info("/mypage/email 진입");

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    log.warn("검증 실패 field={}, message={}",
                            error.getField(),
                            error.getDefaultMessage())

            );
            return "redirect:/mypage";
        }

        try {
            userService.updateEmail(principal.getName(), req.getEmail());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("이메일 변경 실패: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage";
        }
        redirectAttributes.addFlashAttribute("message", "이메일 변경 완료.");

        log.info("이메일 변경 완료");

        return "redirect:/mypage";
    }
    @PostMapping("/mypage/password")
    public String updatePassword(@Valid PasswordChangeRequest req, BindingResult bindingResult, RedirectAttributes redirectAttributes, Principal principal) {
        log.info("/mypage/password 진입");

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    log.warn("검증 실패 field={}, message={}",
                            error.getField(),
                            error.getDefaultMessage())

            );
            return "redirect:/mypage";
        }

        try {
            userService.changePassword(principal.getName(), req.getCurrentPassword(), req.getNewPassword());
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("비밀번호 변경 실패: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/mypage";
        }
        redirectAttributes.addFlashAttribute("message", "비밀번호 변경 완료.");

        log.info("비밀번호 변경 완료");

        return "redirect:/mypage";
    }
}
