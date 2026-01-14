package com.example.portfolio.controller;

import com.example.portfolio.domain.CityMap;
import com.example.portfolio.dto.CityMapResponse;
import com.example.portfolio.dto.CityMapUpdateRequest;
import com.example.portfolio.service.CityMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class CityMapApiController {

    private final CityMapService cityMapService;

    /**
     * 사용자의 첫 번째 도시 조회 (기존 호환성)
     */
    @GetMapping("/{username}")
    public ResponseEntity<?> getMap(
            @PathVariable String username,
            Principal principal) {
        try {
            String currentUsername = principal != null ? principal.getName() : null;
            CityMapResponse response = cityMapService.getMapByUsername(username, currentUsername);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("맵 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 최근 플레이한 도시 정보 조회
     */
    @GetMapping("/{username}/last-played")
    public ResponseEntity<?> getLastPlayedCity(
            @PathVariable String username,
            Principal principal) {
        // 본인만 조회 가능
        if (principal == null || !username.equals(principal.getName())) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            Map<String, Object> result = cityMapService.getLastPlayedCityInfo(username);
            if (result == null) {
                return ResponseEntity.ok(Map.of("exists", false));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("최근 플레이 도시 조회 실패: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("exists", false));
        }
    }

    /**
     * 사용자의 모든 도시 목록 조회 (cityName 패턴보다 먼저 선언)
     */
    @GetMapping("/{username}/cities")
    public ResponseEntity<?> getCities(
            @PathVariable String username,
            Principal principal) {
        try {
            List<CityMapResponse> cities = cityMapService.getCitiesByUsername(username);
            return ResponseEntity.ok(cities);
        } catch (IllegalArgumentException e) {
            log.warn("도시 목록 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 도시 조회 (도시 이름으로)
     */
    @GetMapping("/{username}/{cityName}")
    public ResponseEntity<?> getMapByCityName(
            @PathVariable String username,
            @PathVariable String cityName,
            Principal principal) {
        try {
            String currentUsername = principal != null ? principal.getName() : null;
            CityMapResponse response = cityMapService.getMapByUsernameAndCityName(username, cityName, currentUsername);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("맵 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 첫 번째 도시 저장 (기존 호환성)
     */
    @PutMapping("/{username}")
    public ResponseEntity<?> updateMap(
            @PathVariable String username,
            @Valid @RequestBody CityMapUpdateRequest request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            CityMapResponse response = cityMapService.updateMap(
                    username, null, request, principal.getName());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("맵 저장 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(Map.of("error", "자신의 맵만 수정할 수 있습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("맵 저장 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 특정 도시 저장 (도시 이름으로)
     */
    @PutMapping("/{username}/{cityName}")
    public ResponseEntity<?> updateMapByCityName(
            @PathVariable String username,
            @PathVariable String cityName,
            @Valid @RequestBody CityMapUpdateRequest request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            CityMapResponse response = cityMapService.updateMap(
                    username, cityName, request, principal.getName());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("맵 저장 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(403)
                    .body(Map.of("error", "자신의 맵만 수정할 수 있습니다"));
        } catch (IllegalArgumentException e) {
            log.warn("맵 저장 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 새 도시 생성
     */
    @PostMapping("/{username}/cities")
    public ResponseEntity<?> createCity(
            @PathVariable String username,
            @RequestBody Map<String, String> request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        if (!username.equals(principal.getName())) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "자신의 도시만 생성할 수 있습니다"));
        }

        try {
            String cityName = request.getOrDefault("cityName", "New City");
            CityMap newCity = cityMapService.createNewCity(username, cityName);
            return ResponseEntity.ok(Map.of(
                "id", newCity.getId(),
                "cityName", newCity.getCityName(),
                "slug", newCity.getSlug(),
                "message", "도시가 생성되었습니다"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 도시 삭제
     */
    @DeleteMapping("/{username}/{cityName}")
    public ResponseEntity<?> deleteCity(
            @PathVariable String username,
            @PathVariable String cityName,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            cityMapService.deleteCity(username, cityName, principal.getName());
            return ResponseEntity.ok(Map.of("message", "도시가 삭제되었습니다"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 세금 수집 API
     */
    @PostMapping("/{username}/collect-tax")
    public ResponseEntity<?> collectTax(
            @PathVariable String username,
            @RequestParam(required = false) String cityName,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            CityMapResponse response = cityMapService.collectTax(username, cityName, principal.getName());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 로그인 보상 수령 API
     */
    @PostMapping("/{username}/claim-reward")
    public ResponseEntity<?> claimLoginReward(
            @PathVariable String username,
            @RequestParam(required = false) String cityName,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            CityMapResponse response = cityMapService.claimLoginReward(username, cityName, principal.getName());
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
