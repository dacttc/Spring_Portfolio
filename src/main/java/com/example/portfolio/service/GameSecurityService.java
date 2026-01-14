package com.example.portfolio.service;

import com.example.portfolio.domain.CellType;
import com.example.portfolio.domain.CityMap;
import com.example.portfolio.dto.CityMapUpdateRequest;
import com.example.portfolio.dto.CityStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameSecurityService {

    private final CityStatsService cityStatsService;

    // Rate limiting: 사용자별 마지막 요청 시간
    private final Map<String, LocalDateTime> lastRequestTime = new ConcurrentHashMap<>();
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    // 보안 설정
    private static final boolean TEST_MODE = true;  // TODO: 테스트 완료 후 false로 변경
    private static final int RATE_LIMIT_REQUESTS = 30;  // 분당 최대 요청 수
    private static final int RATE_LIMIT_WINDOW_SECONDS = 60;
    private static final long MAX_MONEY_INCREASE_PER_SAVE = 50000;  // 저장당 최대 증가 가능 금액
    private static final String HMAC_SECRET = "city-builder-secret-key-2024";  // 실제로는 환경변수로

    /**
     * Rate Limiting 체크
     */
    public boolean checkRateLimit(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRequest = lastRequestTime.get(username);

        if (lastRequest == null) {
            lastRequestTime.put(username, now);
            requestCounts.put(username, 1);
            return true;
        }

        Duration elapsed = Duration.between(lastRequest, now);
        if (elapsed.getSeconds() > RATE_LIMIT_WINDOW_SECONDS) {
            // 윈도우 리셋
            lastRequestTime.put(username, now);
            requestCounts.put(username, 1);
            return true;
        }

        int count = requestCounts.getOrDefault(username, 0) + 1;
        if (count > RATE_LIMIT_REQUESTS) {
            log.warn("Rate limit exceeded for user: {}", username);
            return false;
        }

        requestCounts.put(username, count);
        return true;
    }

    /**
     * 맵 데이터 무결성 검증
     */
    public ValidationResult validateMapUpdate(CityMap currentMap, CityMapUpdateRequest request) {
        // 테스트 모드: 모든 검증 스킵
        if (TEST_MODE) {
            return ValidationResult.success();
        }

        // 1. 돈 증가량 검증
        Long currentMoney = currentMap.getMoney();
        Long requestedMoney = request.getMoney();

        if (requestedMoney > currentMoney) {
            // 서버에서 계산한 예상 수입과 비교
            CityStatsResponse stats = cityStatsService.calculateStats(currentMap);
            long maxPossibleEarnings = stats.getTaxPerHour() * 24 + MAX_MONEY_INCREASE_PER_SAVE;

            if (requestedMoney - currentMoney > maxPossibleEarnings) {
                log.warn("Suspicious money increase detected: {} -> {} (max allowed: {})",
                    currentMoney, requestedMoney, currentMoney + maxPossibleEarnings);
                return ValidationResult.fail("비정상적인 자금 증가가 감지되었습니다");
            }
        }

        // 2. 그리드 검증
        int[][] grid = request.getGrid();
        int[][] currentGrid = parseGrid(currentMap.getGridData());

        // 2-1. 잠긴 셀 변경 검증 (2차선 및 4차선 잠긴 도로 모두)
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 48; y++) {
                int currentCell = currentGrid[x][y];
                int newCell = grid[x][y];
                // 잠긴 도로(2차선 또는 4차선)는 수정 불가
                boolean isLockedRoad = currentCell == CellType.LOCKED_ROAD.getCode() ||
                                       currentCell == CellType.LOCKED_ROAD_4LANE.getCode();
                if (isLockedRoad) {
                    // 잠긴 도로는 같은 타입의 잠긴 도로로만 유지 가능
                    boolean isStillLockedRoad = newCell == CellType.LOCKED_ROAD.getCode() ||
                                                newCell == CellType.LOCKED_ROAD_4LANE.getCode();
                    if (!isStillLockedRoad) {
                        log.warn("Attempt to modify locked cell at ({}, {})", x, y);
                        return ValidationResult.fail("잠긴 영역은 수정할 수 없습니다");
                    }
                }
            }
        }

        // 2-2. 비정상적인 건물 배치 검증 (건설 비용 없이 건물 배치)
        int newBuildingCost = calculateNewBuildingCost(currentGrid, grid);
        if (newBuildingCost > currentMoney) {
            log.warn("Building cost exceeds available money: cost={}, money={}",
                newBuildingCost, currentMoney);
            return ValidationResult.fail("건설 비용이 부족합니다");
        }

        // 2-3. 한 번에 너무 많은 변경 감지
        int changedCells = countChangedCells(currentGrid, grid);
        if (changedCells > 100) {  // 한 번에 100칸 이상 변경 불가
            log.warn("Too many cell changes in single request: {}", changedCells);
            return ValidationResult.fail("한 번에 너무 많은 변경이 감지되었습니다");
        }

        return ValidationResult.success();
    }

    /**
     * 데이터 체크섬 생성 (클라이언트 -> 서버 검증용)
     */
    public String generateChecksum(String username, int[][] grid, Long money) {
        try {
            String data = username + ":" + gridToString(grid) + ":" + money;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to generate checksum", e);
            return null;
        }
    }

    /**
     * 체크섬 검증
     */
    public boolean verifyChecksum(String username, int[][] grid, Long money, String providedChecksum) {
        String expectedChecksum = generateChecksum(username, grid, money);
        return expectedChecksum != null && expectedChecksum.equals(providedChecksum);
    }

    /**
     * 이상 행동 감지
     */
    public boolean detectAnomalousActivity(String username, CityMap cityMap) {
        // 1. 비정상적으로 빠른 진행
        if (cityMap.getCreatedAt() != null) {
            Duration playTime = Duration.between(cityMap.getCreatedAt(), LocalDateTime.now());
            if (playTime.toHours() < 1 && cityMap.getPopulation() > 1000) {
                log.warn("Anomalous progress detected for {}: {} population in {} hours",
                    username, cityMap.getPopulation(), playTime.toHours());
                return true;
            }
        }

        // 2. 비정상적인 자금
        if (cityMap.getMoney() > 10_000_000) {  // 1천만 이상
            CityStatsResponse stats = cityStatsService.calculateStats(cityMap);
            Duration playTime = Duration.between(cityMap.getCreatedAt(), LocalDateTime.now());
            long maxPossibleMoney = 10000 + (stats.getTaxPerHour() * playTime.toHours());

            if (cityMap.getMoney() > maxPossibleMoney * 2) {
                log.warn("Anomalous money detected for {}: {} (max expected: {})",
                    username, cityMap.getMoney(), maxPossibleMoney);
                return true;
            }
        }

        return false;
    }

    // === Helper Methods ===

    private int[][] parseGrid(String gridData) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(gridData, int[][].class);
        } catch (Exception e) {
            return new int[48][48];
        }
    }

    private int calculateNewBuildingCost(int[][] oldGrid, int[][] newGrid) {
        int cost = 0;
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 48; y++) {
                if (oldGrid[x][y] != newGrid[x][y]) {
                    CellType newType = CellType.fromCode(newGrid[x][y]);
                    if (newType != null && newType.getBuildCost() > 0) {
                        cost += newType.getBuildCost();
                    }
                }
            }
        }
        return cost;
    }

    private int countChangedCells(int[][] oldGrid, int[][] newGrid) {
        int count = 0;
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 48; y++) {
                if (oldGrid[x][y] != newGrid[x][y]) {
                    count++;
                }
            }
        }
        return count;
    }

    private String gridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row) {
                sb.append(cell).append(",");
            }
        }
        return sb.toString();
    }

    // === Validation Result ===

    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
