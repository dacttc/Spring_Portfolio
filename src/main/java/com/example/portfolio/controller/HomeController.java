package com.example.portfolio.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    // 마스터 계정 목록
    private static final String MASTER_ACCOUNT = "dacttc";

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        String currentUsername = authentication != null ? authentication.getName() : null;
        model.addAttribute("currentUsername", currentUsername);
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }


    @GetMapping("/viewer")
    public String viewer() {
        return "viewer"; // templates/viewer.html
    }

    @GetMapping("/map-editor")
    public String mapEditor(Authentication authentication) {
        // 마스터 계정만 접근 가능
        if (authentication == null || !MASTER_ACCOUNT.equals(authentication.getName())) {
            return "redirect:/";
        }
        return "map-editor";
    }

}
