package com.example.portfolio.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CityStatsResponse {
    private final int population;
    private final int happiness;
    private final int powerCapacity;
    private final int powerUsage;
    private final int crimeRate;
    private final int fireRisk;
    private final int trafficLevel;
    private final int taxPerHour;

    private final int roadCount;
    private final int residentialCount;
    private final int commercialCount;
    private final int industrialCount;

    // 도로별 혼잡도 맵 (시각화용)
    private final int[][] congestionMap;

    public boolean hasPowerShortage() {
        return powerUsage > powerCapacity;
    }

    public int getPowerBalance() {
        return powerCapacity - powerUsage;
    }

    public String getTrafficStatus() {
        if (trafficLevel < 30) return "원활";
        if (trafficLevel < 60) return "보통";
        if (trafficLevel < 80) return "혼잡";
        return "정체";
    }

    public String getTrafficColor() {
        if (trafficLevel < 30) return "#00ff88";  // 초록
        if (trafficLevel < 60) return "#ffcc00";  // 노랑
        if (trafficLevel < 80) return "#ff9500";  // 주황
        return "#ff4444";  // 빨강
    }
}
