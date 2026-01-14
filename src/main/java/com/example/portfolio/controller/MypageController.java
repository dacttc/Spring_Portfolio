package com.example.portfolio.controller;


import com.example.portfolio.domain.User;
import com.example.portfolio.repository.UserRepository;
import com.example.portfolio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor

public class MypageController {


    private final UserService userService;
    @GetMapping("/mypage")
    public String mypage(Principal principal, Model model) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("로그인 사용자를 DB에서 찾을 수 없습니다."));

        model.addAttribute("user", user);  // 템플릿에 user 전달
        return "mypage";
    }

}
