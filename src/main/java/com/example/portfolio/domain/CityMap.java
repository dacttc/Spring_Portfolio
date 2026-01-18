package com.example.portfolio.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "city_maps")
public class CityMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String cityName = "My City";  // 도시 이름

    @Column(nullable = false, length = 50)
    private String slug;  // URL용 슬러그 (도시 고유 식별자)

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String gridData;

    @Column(columnDefinition = "LONGTEXT")
    private String buildingsData;  // 건물 레벨/폐건물 상태 JSON

    @Column(columnDefinition = "TEXT")
    private String cameraState;  // 카메라 위치/타겟 JSON

    @Column(columnDefinition = "TEXT")
    private String gameState;  // 게임 시간/날짜 JSON

    @Column(length = 100)
    private String templateName;  // 사용된 맵 템플릿 이름

    @Column(nullable = false)
    private Long money = 10000L;

    // === 도시 통계 ===
    @Column(nullable = false)
    private Integer population = 0;

    @Column(nullable = false)
    private Integer happiness = 50; // 0-100

    @Column(nullable = false)
    private Integer powerCapacity = 0; // 총 전력 생산량

    @Column(nullable = false)
    private Integer powerUsage = 0; // 총 전력 사용량

    @Column(nullable = false)
    private Integer crimeRate = 0; // 범죄율 0-100

    @Column(nullable = false)
    private Integer fireRisk = 0; // 화재 위험도 0-100

    @Column(nullable = false)
    private Integer trafficLevel = 0; // 교통량 0-100

    // === 세금 시스템 ===
    @Column(nullable = false)
    private Integer hourlyTaxRate = 0; // 시간당 세금 수입 (건물 변경 시 갱신)

    // === 일일 시스템 ===
    @Column(nullable = false)
    private Integer actionPoints = 10; // 일일 행동력

    @Column(nullable = false)
    private Integer consecutiveLoginDays = 0; // 연속 출석일

    @Column
    private LocalDateTime lastCollectedAt; // 마지막 세금 수집 시간

    @Column
    private LocalDateTime lastLoginDate; // 마지막 로그인 날짜 (출석 체크용)

    @Column(nullable = false)
    private Long unclaimedTax = 0L; // 미수령 세금

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public CityMap(User user, String cityName, String slug, String gridData, Long money) {
        this.user = user;
        this.cityName = cityName != null ? cityName : "My City";
        this.slug = slug;
        this.gridData = gridData;
        this.money = money != null ? money : 10000L;
        this.population = 0;
        this.happiness = 50;
        this.powerCapacity = 0;
        this.powerUsage = 0;
        this.crimeRate = 0;
        this.fireRisk = 0;
        this.trafficLevel = 0;
        this.hourlyTaxRate = 0;
        this.actionPoints = 10;
        this.consecutiveLoginDays = 0;
        this.unclaimedTax = 0L;
        this.lastCollectedAt = LocalDateTime.now();
        this.lastLoginDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static CityMap createDefault(User user, String cityName, String slug) {
        int[][] grid = new int[50][50];

        // 하단 경계도로만 (y = 48, 49) - LOCKED_ROAD_4LANE = 14
        for (int x = 0; x < 50; x++) {
            grid[x][48] = 14;
            grid[x][49] = 14;
        }

        return CityMap.builder()
                .user(user)
                .cityName(cityName)
                .slug(slug)
                .gridData(convertGridToJson(grid))
                .money(5000L)
                .build();
    }

    // 기본 도시 생성 (호환성)
    public static CityMap createDefault(User user) {
        return createDefault(user, "My City", "city-1");
    }

    // 템플릿 그리드 데이터로 도시 생성
    public static CityMap createWithGrid(User user, String cityName, String slug, String gridData, String templateName) {
        CityMap cityMap = CityMap.builder()
                .user(user)
                .cityName(cityName)
                .slug(slug)
                .gridData(gridData)
                .money(5000L)
                .build();
        cityMap.setTemplateName(templateName);
        return cityMap;
    }

    private static String convertGridToJson(int[][] grid) {
        StringBuilder sb = new StringBuilder("[");
        for (int x = 0; x < 50; x++) {
            sb.append("[");
            for (int y = 0; y < 50; y++) {
                sb.append(grid[x][y]);
                if (y < 49) sb.append(",");
            }
            sb.append("]");
            if (x < 49) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public void updateMap(String newGridData, Long newMoney, Integer newHourlyTaxRate) {
        this.gridData = newGridData;
        this.money = newMoney;
        this.hourlyTaxRate = newHourlyTaxRate;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMap(String newGridData, Long newMoney, Integer newHourlyTaxRate, String newBuildingsData) {
        this.gridData = newGridData;
        this.money = newMoney;
        this.hourlyTaxRate = newHourlyTaxRate;
        this.buildingsData = newBuildingsData;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMap(String newGridData, Long newMoney, Integer newHourlyTaxRate, String newBuildingsData, String newCameraState) {
        this.gridData = newGridData;
        this.money = newMoney;
        this.hourlyTaxRate = newHourlyTaxRate;
        this.buildingsData = newBuildingsData;
        this.cameraState = newCameraState;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateMap(String newGridData, Long newMoney, Integer newHourlyTaxRate, String newBuildingsData, String newCameraState, String newGameState) {
        this.gridData = newGridData;
        this.money = newMoney;
        this.hourlyTaxRate = newHourlyTaxRate;
        this.buildingsData = newBuildingsData;
        this.cameraState = newCameraState;
        this.gameState = newGameState;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStats(Integer population, Integer happiness, Integer powerCapacity,
                           Integer powerUsage, Integer crimeRate, Integer fireRisk, Integer trafficLevel) {
        this.population = population;
        this.happiness = Math.max(0, Math.min(100, happiness));
        this.powerCapacity = powerCapacity;
        this.powerUsage = powerUsage;
        this.crimeRate = Math.max(0, Math.min(100, crimeRate));
        this.fireRisk = Math.max(0, Math.min(100, fireRisk));
        this.trafficLevel = Math.max(0, Math.min(100, trafficLevel));
        this.updatedAt = LocalDateTime.now();
    }

    public void collectTax(Long amount) {
        this.money += amount;
        this.unclaimedTax = 0L;
        this.lastCollectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addUnclaimedTax(Long amount) {
        this.unclaimedTax += amount;
    }

    public boolean useActionPoints(int amount) {
        if (this.actionPoints >= amount) {
            this.actionPoints -= amount;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public void resetDailyActionPoints() {
        this.actionPoints = 10;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLoginStreak(LocalDateTime now) {
        if (this.lastLoginDate == null) {
            this.consecutiveLoginDays = 1;
        } else {
            long daysBetween = java.time.Duration.between(
                this.lastLoginDate.toLocalDate().atStartOfDay(),
                now.toLocalDate().atStartOfDay()
            ).toDays();

            if (daysBetween == 1) {
                this.consecutiveLoginDays++;
            } else if (daysBetween > 1) {
                this.consecutiveLoginDays = 1;
            }
            // daysBetween == 0 이면 같은 날 재접속, 변경 없음
        }
        this.lastLoginDate = now;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPowerShortage() {
        return powerUsage > powerCapacity;
    }

    public int getPowerBalance() {
        return powerCapacity - powerUsage;
    }
}
