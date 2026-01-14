package com.example.portfolio.controller;

import com.example.portfolio.service.CityMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.Principal;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CityController {

    private final CityMapService cityMapService;

    // 예약된 경로 목록 - 이 경로들은 /{username}으로 처리되면 안 됨
    private static final Set<String> RESERVED_PATHS = Set.of(
            "login", "signup", "mypage", "viewer", "admin",
            "verify", "resend-verification", "api", "models",
            "css", "js", "test-email", "logout", "error"
    );

    @GetMapping("/{username}")
    public String viewCity(
            @PathVariable String username,
            Principal principal,
            Model model) {

        // 예약된 경로인지 확인
        if (RESERVED_PATHS.contains(username.toLowerCase())) {
            return "redirect:/";
        }

        try {
            // 사용자가 존재하는지 확인
            if (!cityMapService.userExists(username)) {
                return "redirect:/?error=user_not_found";
            }

            String currentUsername = principal != null ? principal.getName() : null;
            boolean isOwner = username.equals(currentUsername);

            model.addAttribute("mapUsername", username);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("currentUsername", currentUsername);
            model.addAttribute("cityName", null);  // 기본 도시

            return "city";
        } catch (Exception e) {
            log.error("Error loading city for user: {}", username, e);
            return "redirect:/?error=load_failed";
        }
    }

    // cityName 패턴: 파일 확장자(.js, .css 등)가 없는 경로만 매칭
    @GetMapping(value = "/{username}/{cityName:^[^.]*$}", produces = "text/html")
    public String viewCityByCityName(
            @PathVariable String username,
            @PathVariable String cityName,
            Principal principal,
            Model model) {

        // 예약된 경로인지 확인
        if (RESERVED_PATHS.contains(username.toLowerCase())) {
            return "redirect:/";
        }

        // cities는 도시 목록 페이지가 아닌 API 경로
        if ("cities".equals(cityName)) {
            return "redirect:/" + username;
        }

        try {
            // 사용자가 존재하는지 확인
            if (!cityMapService.userExists(username)) {
                return "redirect:/?error=user_not_found";
            }

            String currentUsername = principal != null ? principal.getName() : null;
            boolean isOwner = username.equals(currentUsername);

            model.addAttribute("mapUsername", username);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("currentUsername", currentUsername);
            model.addAttribute("cityName", cityName);

            return "city";
        } catch (Exception e) {
            log.error("Error loading city {} for user: {}", cityName, username, e);
            return "redirect:/?error=load_failed";
        }
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {
        log.error("Unhandled exception in CityController", e);
        return "redirect:/?error=unexpected";
    }
}
