package com.example.portfolio.service;

import com.example.portfolio.domain.CellType;
import com.example.portfolio.domain.CityMap;
import com.example.portfolio.dto.CityStatsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CityStatsService {

    private final ObjectMapper objectMapper;
    private static final int GRID_SIZE = 48;
    private static final int MAX_OFFLINE_HOURS = 24;

    /**
     * 그리드 데이터를 분석하여 도시 통계 계산
     */
    public CityStatsResponse calculateStats(CityMap cityMap) {
        int[][] grid = parseGrid(cityMap.getGridData());

        // 건물별 개수 및 위치
        List<int[]> powerPlants = new ArrayList<>();
        List<int[]> policeStations = new ArrayList<>();
        List<int[]> fireStations = new ArrayList<>();
        List<int[]> parks = new ArrayList<>();
        List<int[]> schools = new ArrayList<>();
        List<int[]> hospitals = new ArrayList<>();

        int totalPopulation = 0;
        int totalPowerCapacity = 0;
        int totalPowerUsage = 0;
        int totalTaxPerHour = 0;
        int totalTrafficImpact = 0;
        int roadCount = 0;
        int residentialCount = 0;
        int commercialCount = 0;
        int industrialCount = 0;

        // 1차 스캔: 건물 위치 및 기본 통계
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                CellType type = CellType.fromCode(grid[x][y]);

                if (type.isRoad()) {
                    roadCount++;
                    continue;
                }

                // 인구
                totalPopulation += type.getPopulationBonus();

                // 전력
                if (type.producesPower()) {
                    totalPowerCapacity += type.getPowerProduction();
                } else {
                    totalPowerUsage += type.getPowerConsumption();
                }

                // 세금
                totalTaxPerHour += type.getTaxPerHour();

                // 교통량
                totalTrafficImpact += type.getTrafficImpact();

                // 건물 위치 저장
                switch (type) {
                    case POWER_PLANT, LARGE_POWER_PLANT -> powerPlants.add(new int[]{x, y});
                    case POLICE_STATION -> policeStations.add(new int[]{x, y});
                    case FIRE_STATION -> fireStations.add(new int[]{x, y});
                    case PARK -> parks.add(new int[]{x, y});
                    case SCHOOL -> schools.add(new int[]{x, y});
                    case HOSPITAL -> hospitals.add(new int[]{x, y});
                    case RESIDENTIAL_LOW, RESIDENTIAL_MID, RESIDENTIAL_HIGH -> residentialCount++;
                    case COMMERCIAL -> commercialCount++;
                    case INDUSTRIAL -> industrialCount++;
                    default -> {}
                }
            }
        }

        // 2차 계산: 커버리지 기반 통계
        boolean[][] policeCoverage = calculateCoverage(policeStations, CellType.POLICE_STATION.getEffectRadius());
        boolean[][] fireCoverage = calculateCoverage(fireStations, CellType.FIRE_STATION.getEffectRadius());
        boolean[][] powerCoverage = calculateCoverage(powerPlants, CellType.POWER_PLANT.getEffectRadius());

        // 치안율 (경찰서 커버리지 기반)
        int coveredByPolice = countCoveredResidential(grid, policeCoverage);
        int crimeRate = residentialCount > 0
            ? 100 - (coveredByPolice * 100 / Math.max(1, residentialCount))
            : 0;

        // 화재 위험도 (소방서 커버리지 기반)
        int coveredByFire = countCoveredResidential(grid, fireCoverage);
        int fireRisk = residentialCount > 0
            ? 100 - (coveredByFire * 100 / Math.max(1, residentialCount))
            : 0;

        // 교통량 (도로 대비 건물 비율)
        int buildingCount = residentialCount + commercialCount + industrialCount;
        int trafficLevel = roadCount > 0
            ? Math.min(100, (buildingCount * 100) / (roadCount * 2) + totalTrafficImpact)
            : 0;

        // 행복도 계산
        int happiness = calculateHappiness(
            cityMap.getHappiness(),
            crimeRate,
            fireRisk,
            trafficLevel,
            parks.size(),
            totalPowerCapacity >= totalPowerUsage
        );

        // 도로 혼잡도 맵 생성 (시각화용)
        int[][] congestionMap = calculateCongestionMap(grid, roadCount, buildingCount);

        return CityStatsResponse.builder()
                .population(totalPopulation)
                .happiness(happiness)
                .powerCapacity(totalPowerCapacity)
                .powerUsage(totalPowerUsage)
                .crimeRate(crimeRate)
                .fireRisk(fireRisk)
                .trafficLevel(trafficLevel)
                .taxPerHour(totalTaxPerHour)
                .roadCount(roadCount)
                .residentialCount(residentialCount)
                .commercialCount(commercialCount)
                .industrialCount(industrialCount)
                .congestionMap(congestionMap)
                .build();
    }

    /**
     * 그리드에서 시간당 세금만 빠르게 계산 (맵 저장 시 사용)
     */
    public int calculateHourlyTaxRate(String gridData) {
        int[][] grid = parseGrid(gridData);
        int totalTaxPerHour = 0;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                CellType type = CellType.fromCode(grid[x][y]);
                totalTaxPerHour += type.getTaxPerHour();
            }
        }
        return totalTaxPerHour;
    }

    /**
     * 오프라인 수익 계산 (저장된 hourlyTaxRate 사용으로 최적화)
     */
    public long calculateOfflineEarnings(CityMap cityMap) {
        if (cityMap.getLastCollectedAt() == null) {
            return 0;
        }

        Duration offlineTime = Duration.between(cityMap.getLastCollectedAt(), LocalDateTime.now());
        long hours = Math.min(offlineTime.toHours(), MAX_OFFLINE_HOURS);

        if (hours <= 0) {
            return 0;
        }

        // 저장된 hourlyTaxRate 사용 (그리드 파싱 불필요)
        int hourlyTaxRate = cityMap.getHourlyTaxRate();

        // hourlyTaxRate가 0이면 기존 방식으로 계산 (마이그레이션 호환)
        if (hourlyTaxRate == 0) {
            hourlyTaxRate = calculateHourlyTaxRate(cityMap.getGridData());
        }

        // 기본 세금 수입
        long baseTax = (long) hourlyTaxRate * hours;

        // 전력 부족 시 50% 감소
        if (cityMap.hasPowerShortage()) {
            baseTax = baseTax / 2;
        }

        // 행복도에 따른 보너스/페널티 (-20% ~ +20%)
        double happinessMultiplier = 0.8 + (cityMap.getHappiness() * 0.004);
        baseTax = (long) (baseTax * happinessMultiplier);

        // 교통량에 따른 페널티 (최대 -30%)
        double trafficPenalty = 1.0 - (cityMap.getTrafficLevel() * 0.003);
        baseTax = (long) (baseTax * trafficPenalty);

        return Math.max(0, baseTax);
    }

    /**
     * 일일 출석 보상 계산
     */
    public long calculateLoginReward(int consecutiveDays) {
        return switch (consecutiveDays) {
            case 1 -> 1000;
            case 2 -> 1500;
            case 3 -> 5000;
            case 4, 5, 6 -> 3000;
            case 7 -> 10000; // + 특별 건물
            default -> {
                if (consecutiveDays >= 30) yield 20000;
                else if (consecutiveDays >= 14) yield 10000;
                else yield 3000 + (consecutiveDays - 7) * 500;
            }
        };
    }

    // === Private Helper Methods ===

    private int[][] parseGrid(String gridData) {
        try {
            return objectMapper.readValue(gridData, int[][].class);
        } catch (JsonProcessingException e) {
            return new int[GRID_SIZE][GRID_SIZE];
        }
    }

    private boolean[][] calculateCoverage(List<int[]> buildings, int radius) {
        boolean[][] coverage = new boolean[GRID_SIZE][GRID_SIZE];

        for (int[] pos : buildings) {
            int cx = pos[0], cy = pos[1];
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int nx = cx + dx, ny = cy + dy;
                    if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                        if (dx * dx + dy * dy <= radius * radius) {
                            coverage[nx][ny] = true;
                        }
                    }
                }
            }
        }
        return coverage;
    }

    private int countCoveredResidential(int[][] grid, boolean[][] coverage) {
        int count = 0;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                CellType type = CellType.fromCode(grid[x][y]);
                if (type.isResidential() && coverage[x][y]) {
                    count++;
                }
            }
        }
        return count;
    }

    private int calculateHappiness(int baseHappiness, int crimeRate, int fireRisk,
                                   int trafficLevel, int parkCount, boolean hasPower) {
        int happiness = baseHappiness;

        // 범죄율 영향 (최대 -30)
        happiness -= crimeRate * 30 / 100;

        // 화재 위험 영향 (최대 -20)
        happiness -= fireRisk * 20 / 100;

        // 교통 혼잡 영향 (최대 -25)
        happiness -= trafficLevel * 25 / 100;

        // 공원 보너스 (공원당 +3, 최대 +30)
        happiness += Math.min(30, parkCount * 3);

        // 정전 페널티
        if (!hasPower) {
            happiness -= 20;
        }

        return Math.max(0, Math.min(100, happiness));
    }

    private int[][] calculateCongestionMap(int[][] grid, int roadCount, int buildingCount) {
        int[][] congestion = new int[GRID_SIZE][GRID_SIZE];

        if (roadCount == 0) return congestion;

        // 기본 혼잡도
        int baseCongestion = Math.min(100, (buildingCount * 50) / roadCount);

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                CellType type = CellType.fromCode(grid[x][y]);
                if (type.isRoad()) {
                    // 주변 건물 수에 따른 혼잡도
                    int nearbyBuildings = countNearbyBuildings(grid, x, y, 3);
                    int localCongestion = baseCongestion + nearbyBuildings * 5;
                    congestion[x][y] = Math.min(100, localCongestion);
                }
            }
        }
        return congestion;
    }

    private int countNearbyBuildings(int[][] grid, int cx, int cy, int radius) {
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = cx + dx, ny = cy + dy;
                if (nx >= 0 && nx < GRID_SIZE && ny >= 0 && ny < GRID_SIZE) {
                    CellType type = CellType.fromCode(grid[nx][ny]);
                    if (type.isResidential() || type == CellType.COMMERCIAL || type == CellType.INDUSTRIAL) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
