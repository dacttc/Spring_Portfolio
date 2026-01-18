package com.example.portfolio.dto;

import com.example.portfolio.domain.CityMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

@Getter
public class CityMapResponse {
    private final String username;
    private final String cityName;
    private final String slug;
    private final int[][] grid;
    private final Long money;
    private final boolean isOwner;

    // 건물 데이터 (레벨, 폐건물 상태)
    private final Object buildings;

    // 도시 통계
    private final Integer population;
    private final Integer happiness;
    private final Integer powerCapacity;
    private final Integer powerUsage;
    private final Integer crimeRate;
    private final Integer fireRisk;
    private final Integer trafficLevel;

    // 일일 시스템
    private final Integer actionPoints;
    private final Integer consecutiveLoginDays;
    private final Long unclaimedTax;

    // 계산된 통계 (CityStatsResponse에서)
    private final Integer taxPerHour;
    private final int[][] congestionMap;

    // 오프라인 수익
    private final Long offlineEarnings;
    private final Long loginReward;

    // 카메라 상태
    private final Object cameraState;

    // 게임 상태 (시간/날짜)
    private final Object gameState;

    // 사용된 맵 템플릿 이름
    private final String templateName;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public CityMapResponse(CityMap cityMap, boolean isOwner) {
        this(cityMap, isOwner, null, null, 0, 0L, 0L);
    }

    public CityMapResponse(CityMap cityMap, boolean isOwner, CityStatsResponse stats,
                          int[][] congestionMap, int taxPerHour,
                          Long offlineEarnings, Long loginReward) {
        this.username = cityMap.getUser().getUsername();
        // cityName이 null이거나 빈 문자열이면 기본값 사용
        String name = cityMap.getCityName();
        this.cityName = (name != null && !name.trim().isEmpty()) ? name : "My City";
        this.slug = cityMap.getSlug();
        this.grid = parseGridData(cityMap.getGridData());
        this.money = cityMap.getMoney();
        this.isOwner = isOwner;

        // 건물 데이터
        this.buildings = parseBuildingsData(cityMap.getBuildingsData());

        // 엔티티 통계
        this.population = cityMap.getPopulation();
        this.happiness = cityMap.getHappiness();
        this.powerCapacity = cityMap.getPowerCapacity();
        this.powerUsage = cityMap.getPowerUsage();
        this.crimeRate = cityMap.getCrimeRate();
        this.fireRisk = cityMap.getFireRisk();
        this.trafficLevel = cityMap.getTrafficLevel();

        // 일일 시스템
        this.actionPoints = cityMap.getActionPoints();
        this.consecutiveLoginDays = cityMap.getConsecutiveLoginDays();
        this.unclaimedTax = cityMap.getUnclaimedTax();

        // 계산된 통계
        this.taxPerHour = taxPerHour;
        this.congestionMap = congestionMap;

        // 오프라인 수익
        this.offlineEarnings = offlineEarnings;
        this.loginReward = loginReward;

        // 카메라 상태
        this.cameraState = parseCameraState(cityMap.getCameraState());

        // 게임 상태 (시간/날짜)
        this.gameState = parseGameState(cityMap.getGameState());

        // 템플릿 이름
        this.templateName = cityMap.getTemplateName();
    }

    private int[][] parseGridData(String gridData) {
        try {
            // 먼저 직접 2D 배열로 파싱 시도 (기존 형식)
            return objectMapper.readValue(gridData, int[][].class);
        } catch (JsonProcessingException e) {
            // 실패하면 새 형식 (tiles + env) 시도
            try {
                var node = objectMapper.readTree(gridData);
                if (node.has("tiles")) {
                    return objectMapper.treeToValue(node.get("tiles"), int[][].class);
                }
            } catch (JsonProcessingException ex) {
                // 무시
            }
            return new int[50][50];
        }
    }

    private Object parseBuildingsData(String buildingsData) {
        if (buildingsData == null || buildingsData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(buildingsData, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Object parseCameraState(String cameraState) {
        if (cameraState == null || cameraState.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(cameraState, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Object parseGameState(String gameState) {
        if (gameState == null || gameState.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(gameState, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
