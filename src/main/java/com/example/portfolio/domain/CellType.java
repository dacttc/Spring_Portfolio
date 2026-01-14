package com.example.portfolio.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CellType {
    // 기본
    EMPTY(0, "빈 땅", 0, 0, 0, 0),
    ROAD(1, "도로", 0, 0, 0, 0),
    LOCKED_ROAD(2, "외곽 도로", 0, 0, 0, 0),

    // 주거 구역 (시민 자동 생성)
    RESIDENTIAL_LOW(3, "하류층 주거", 5, 1, 100, 0),      // 인구 5, 전력 1, 세금 100/시간
    RESIDENTIAL_MID(4, "중류층 주거", 8, 2, 300, 0),      // 인구 8, 전력 2, 세금 300/시간
    RESIDENTIAL_HIGH(5, "상류층 주거", 10, 3, 800, 0),    // 인구 10, 전력 3, 세금 800/시간

    // 상업/공업 구역 (시민 자동 생성)
    COMMERCIAL(6, "상업 구역", 0, 2, 200, 10),            // 전력 2, 세금 200, 교통량 +10
    INDUSTRIAL(7, "공업 구역", 0, 3, 150, 15),            // 전력 3, 세금 150, 교통량 +15

    // 구역 지정 (플레이어가 지정, 시민이 건물 건설)
    ZONE_RESIDENTIAL(8, "주거 구역 지정", 0, 0, 0, 0),
    ZONE_COMMERCIAL(9, "상업 구역 지정", 0, 0, 0, 0),
    ZONE_INDUSTRIAL(10, "공업 구역 지정", 0, 0, 0, 0),

    // 수로 시스템
    WATER(11, "수로", 0, 0, 0, 0),
    BRIDGE(12, "다리", 0, 0, 0, 0),

    // 4차로 도로
    ROAD_4LANE(13, "4차로 도로", 0, 0, 0, 0),
    LOCKED_ROAD_4LANE(14, "잠긴 4차로 도로", 0, 0, 0, 0),

    // 공공시설 (플레이어 건설)
    POWER_PLANT(20, "발전소", 0, -50, 0, 5),              // 전력 생산 50, 교통량 +5
    POLICE_STATION(21, "경찰서", 0, 2, 0, 3),             // 전력 2, 치안 범위 8칸
    FIRE_STATION(22, "소방서", 0, 2, 0, 3),               // 전력 2, 소방 범위 8칸
    PARK(23, "공원", 0, 0, 0, -5),                        // 행복도 +10, 교통량 -5
    SCHOOL(24, "학교", 0, 3, 0, 8),                       // 전력 3, 중류층 전환율 증가
    HOSPITAL(25, "병원", 0, 5, 0, 10),                    // 전력 5, 상류층 전환율 증가

    // 대형 시설 (해금 필요)
    LARGE_POWER_PLANT(30, "대형 발전소", 0, -150, 0, 10), // 전력 생산 150
    SUBWAY_STATION(31, "지하철역", 0, 5, 0, -20),         // 교통량 대폭 감소
    AIRPORT(32, "공항", 0, 20, 500, 30),                  // 대규모 수입, 교통량 증가
    LANDMARK(33, "랜드마크", 0, 10, 0, 20);               // 행복도 대폭 증가

    private final int code;
    private final String name;
    private final int populationBonus;
    private final int powerUsage; // 음수면 전력 생산
    private final int taxPerHour;
    private final int trafficImpact;

    public static CellType fromCode(int code) {
        for (CellType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return EMPTY;
    }

    public boolean isRoad() {
        return this == ROAD || this == LOCKED_ROAD || this == ROAD_4LANE || this == LOCKED_ROAD_4LANE || this == BRIDGE;
    }

    public boolean isResidential() {
        return this == RESIDENTIAL_LOW || this == RESIDENTIAL_MID || this == RESIDENTIAL_HIGH;
    }

    public boolean isZone() {
        return this == ZONE_RESIDENTIAL || this == ZONE_COMMERCIAL || this == ZONE_INDUSTRIAL;
    }

    public boolean isPublicBuilding() {
        return code >= 20 && code < 40;
    }

    public boolean producesPower() {
        return powerUsage < 0;
    }

    public int getPowerProduction() {
        return powerUsage < 0 ? -powerUsage : 0;
    }

    public int getPowerConsumption() {
        return powerUsage > 0 ? powerUsage : 0;
    }

    // 건물 건설 비용
    public int getBuildCost() {
        return switch (this) {
            case ROAD -> 50;
            case ROAD_4LANE -> 100;
            case BRIDGE -> 75;
            case WATER -> 50;
            case ZONE_RESIDENTIAL, ZONE_COMMERCIAL, ZONE_INDUSTRIAL -> 100;
            case POWER_PLANT -> 5000;
            case POLICE_STATION -> 3000;
            case FIRE_STATION -> 3000;
            case PARK -> 1000;
            case SCHOOL -> 4000;
            case HOSPITAL -> 6000;
            case LARGE_POWER_PLANT -> 15000;
            case SUBWAY_STATION -> 20000;
            case AIRPORT -> 100000;
            case LANDMARK -> 50000;
            default -> 0;
        };
    }

    // 건설에 필요한 AP
    public int getApCost() {
        return switch (this) {
            case ROAD -> 0; // 도로는 AP 안 씀
            case ZONE_RESIDENTIAL, ZONE_COMMERCIAL, ZONE_INDUSTRIAL -> 1;
            case POWER_PLANT, POLICE_STATION, FIRE_STATION, PARK -> 2;
            case SCHOOL, HOSPITAL -> 2;
            case LARGE_POWER_PLANT, SUBWAY_STATION -> 3;
            case AIRPORT, LANDMARK -> 5;
            default -> 0;
        };
    }

    // 효과 범위
    public int getEffectRadius() {
        return switch (this) {
            case POWER_PLANT -> 10;
            case LARGE_POWER_PLANT -> 15;
            case POLICE_STATION, FIRE_STATION -> 8;
            case PARK -> 5;
            case SCHOOL, HOSPITAL -> 6;
            case SUBWAY_STATION -> 12;
            default -> 0;
        };
    }
}
