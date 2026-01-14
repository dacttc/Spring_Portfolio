package com.example.portfolio.service;

import com.example.portfolio.domain.CellType;
import com.example.portfolio.domain.CityMap;
import com.example.portfolio.domain.User;
import com.example.portfolio.dto.CityMapResponse;
import com.example.portfolio.dto.CityMapUpdateRequest;
import com.example.portfolio.dto.CityStatsResponse;
import com.example.portfolio.repository.CityMapRepository;
import com.example.portfolio.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CityMapService {

    private final CityMapRepository cityMapRepository;
    private final UserRepository userRepository;
    private final CityStatsService cityStatsService;
    private final GameSecurityService gameSecurityService;
    private final ObjectMapper objectMapper;

    private static final int GRID_SIZE = 48;
    private static final int MAX_CITIES_PER_USER = 5;  // 유저당 최대 도시 수

    @Transactional(readOnly = true)
    public CityMapResponse getMapByUsername(String username, String currentUsername) {
        return getMapByUsernameAndCityName(username, null, currentUsername);
    }

    @Transactional(readOnly = true)
    public CityMapResponse getMapByUsernameAndCityName(String username, String cityName, String currentUsername) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        CityMap cityMap;
        if (cityName != null && !cityName.isEmpty()) {
            // 특정 도시 이름 요청
            cityMap = cityMapRepository.findByUserAndCityName(user, cityName).orElse(null);

            // "My City"로 요청했는데 없으면, 기본 도시(첫 번째 도시) 반환
            if (cityMap == null && "My City".equals(cityName)) {
                cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
            }

            if (cityMap == null) {
                throw new IllegalArgumentException("도시를 찾을 수 없습니다: " + cityName);
            }
        } else {
            // 기본 도시 요청 - 없으면 새로 생성
            cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
            if (cityMap == null) {
                cityMap = createDefaultMap(user);
            }
        }

        boolean isOwner = username.equals(currentUsername);

        // 통계 계산 (DB에 저장하지 않고 메모리에서만 계산)
        CityStatsResponse stats = cityStatsService.calculateStats(cityMap);

        long offlineEarnings = 0;
        long loginReward = 0;
        boolean needsUpdate = false;

        // 소유자일 경우 오프라인 수익 및 로그인 보상 계산
        if (isOwner) {
            offlineEarnings = cityStatsService.calculateOfflineEarnings(cityMap);

            // 일일 리셋 체크
            LocalDate today = LocalDate.now();
            LocalDate lastLogin = cityMap.getLastLoginDate() != null
                ? cityMap.getLastLoginDate().toLocalDate()
                : null;

            if (lastLogin == null || !lastLogin.equals(today)) {
                needsUpdate = true;
                loginReward = cityStatsService.calculateLoginReward(
                    calculateNewConsecutiveDays(cityMap, today, lastLogin)
                );
            }
        }

        // 일일 리셋이 필요한 경우만 별도 트랜잭션으로 업데이트
        if (needsUpdate) {
            updateDailyReset(cityMap.getId());
        }

        // 소유자가 도시에 접속하면 최근 플레이한 도시로 기록
        if (isOwner) {
            updateLastPlayedCity(user.getId(), cityMap.getCityName());
        }

        return new CityMapResponse(
            cityMap, isOwner, stats,
            stats.getCongestionMap(),
            stats.getTaxPerHour(),
            offlineEarnings,
            loginReward
        );
    }

    private int calculateNewConsecutiveDays(CityMap cityMap, LocalDate today, LocalDate lastLogin) {
        if (lastLogin == null) {
            return 1;
        }
        long daysBetween = java.time.Duration.between(
            lastLogin.atStartOfDay(),
            today.atStartOfDay()
        ).toDays();

        if (daysBetween == 1) {
            return cityMap.getConsecutiveLoginDays() + 1;
        } else if (daysBetween > 1) {
            return 1;
        }
        return cityMap.getConsecutiveLoginDays();
    }

    @Transactional
    public void updateDailyReset(Long cityMapId) {
        cityMapRepository.findById(cityMapId).ifPresent(cityMap -> {
            cityMap.resetDailyActionPoints();
            cityMap.updateLoginStreak(LocalDateTime.now());
        });
    }

    @Transactional
    public void updateLastPlayedCity(Long userId, String cityName) {
        userRepository.findById(userId).ifPresent(user -> {
            user.updateLastPlayedCity(cityName);
        });
    }

    /**
     * 사용자의 최근 플레이한 도시 정보 조회
     */
    @Transactional(readOnly = true)
    public CityMapResponse getLastPlayedCity(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }

        CityMap cityMap = null;

        // 최근 플레이한 도시가 있으면 그것을 사용
        if (user.getLastPlayedCityName() != null && !user.getLastPlayedCityName().isEmpty()) {
            cityMap = cityMapRepository.findByUserAndCityName(user, user.getLastPlayedCityName()).orElse(null);
        }

        // fallback: 첫 번째 도시
        if (cityMap == null) {
            cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
        }

        if (cityMap == null) {
            return null;
        }

        return new CityMapResponse(cityMap, true);
    }

    /**
     * 사용자의 최근 플레이한 도시 상세 정보 조회 (lastPlayedAt 포함)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLastPlayedCityInfo(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }

        CityMap cityMap = null;

        // 최근 플레이한 도시가 있으면 그것을 사용
        if (user.getLastPlayedCityName() != null && !user.getLastPlayedCityName().isEmpty()) {
            cityMap = cityMapRepository.findByUserAndCityName(user, user.getLastPlayedCityName()).orElse(null);
        }

        // 최근 플레이 기록이 없거나 해당 도시가 삭제된 경우, 첫 번째 도시로 fallback
        if (cityMap == null) {
            cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
        }

        if (cityMap == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("exists", true);
        result.put("cityName", cityMap.getCityName());
        result.put("money", cityMap.getMoney());
        result.put("population", cityMap.getPopulation());
        result.put("lastPlayedAt", user.getLastPlayedAt());

        return result;
    }

    public CityMap createDefaultMap(User user) {
        CityMap cityMap = CityMap.createDefault(user);
        return cityMapRepository.save(cityMap);
    }

    public CityMapResponse updateMap(String username, String cityName, CityMapUpdateRequest request, String currentUsername) {
        if (!username.equals(currentUsername)) {
            throw new SecurityException("자신의 맵만 수정할 수 있습니다");
        }

        // Rate Limiting 체크
        if (!gameSecurityService.checkRateLimit(username)) {
            throw new SecurityException("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        CityMap cityMap;
        if (cityName != null && !cityName.isEmpty()) {
            cityMap = cityMapRepository.findByUserAndCityName(user, cityName).orElse(null);

            // "My City"로 요청했는데 없으면, 기본 도시(첫 번째 도시) 반환
            if (cityMap == null && "My City".equals(cityName)) {
                cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
            }

            if (cityMap == null) {
                throw new IllegalArgumentException("도시를 찾을 수 없습니다: " + cityName);
            }
        } else {
            cityMap = cityMapRepository.findFirstByUser(user)
                    .orElseGet(() -> createDefaultMap(user));
        }

        // 보안 검증
        GameSecurityService.ValidationResult validationResult =
            gameSecurityService.validateMapUpdate(cityMap, request);
        if (!validationResult.isValid()) {
            throw new SecurityException(validationResult.getMessage());
        }

        // 이상 행동 감지
        if (gameSecurityService.detectAnomalousActivity(username, cityMap)) {
            // 로그만 남기고 진행 (또는 차단 가능)
            // throw new SecurityException("비정상적인 활동이 감지되었습니다");
        }

        validateGrid(request.getGrid());

        // 서버에서 돈 계산 (클라이언트 값 사용 안 함)
        Long serverCalculatedMoney = calculateServerMoney(cityMap, request.getGrid());

        String gridJson = convertGridToJson(request.getGrid());

        // 건물 데이터 JSON 변환
        String buildingsJson = null;
        if (request.getBuildings() != null) {
            try {
                buildingsJson = objectMapper.writeValueAsString(request.getBuildings());
            } catch (JsonProcessingException e) {
                // 건물 데이터 변환 실패 시 무시
            }
        }

        // 카메라 상태 JSON 변환
        String cameraStateJson = null;
        if (request.getCameraState() != null) {
            try {
                cameraStateJson = objectMapper.writeValueAsString(request.getCameraState());
            } catch (JsonProcessingException e) {
                // 카메라 상태 변환 실패 시 무시
            }
        }

        // 게임 상태 JSON 변환 (시간/날짜)
        String gameStateJson = null;
        if (request.getGameState() != null) {
            try {
                gameStateJson = objectMapper.writeValueAsString(request.getGameState());
            } catch (JsonProcessingException e) {
                // 게임 상태 변환 실패 시 무시
            }
        }

        // 시간당 세금 계산 및 저장 (오프라인 수익 계산 최적화)
        int hourlyTaxRate = cityStatsService.calculateHourlyTaxRate(gridJson);
        cityMap.updateMap(gridJson, serverCalculatedMoney, hourlyTaxRate, buildingsJson, cameraStateJson, gameStateJson);

        // 명시적으로 저장 (dirty checking 대신)
        cityMapRepository.save(cityMap);

        // 통계는 저장하지 않고 응답용으로만 계산 (DB 쓰기 최소화)
        CityStatsResponse stats = cityStatsService.calculateStats(cityMap);

        return new CityMapResponse(
            cityMap, true, stats,
            stats.getCongestionMap(),
            stats.getTaxPerHour(),
            0L, 0L
        );
    }

    /**
     * 서버에서 돈 계산 (클라이언트 값 신뢰하지 않음)
     */
    private Long calculateServerMoney(CityMap currentMap, int[][] newGrid) {
        Long currentMoney = currentMap.getMoney();
        int[][] oldGrid = parseGrid(currentMap.getGridData());

        // 새로 건설된 건물 비용 차감
        long buildCost = 0;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (oldGrid[x][y] != newGrid[x][y]) {
                    CellType newType = CellType.fromCode(newGrid[x][y]);
                    if (newType != null && newType.getBuildCost() > 0) {
                        buildCost += newType.getBuildCost();
                    }
                }
            }
        }

        return Math.max(0, currentMoney - buildCost);
    }

    private int[][] parseGrid(String gridData) {
        try {
            return objectMapper.readValue(gridData, int[][].class);
        } catch (JsonProcessingException e) {
            return new int[GRID_SIZE][GRID_SIZE];
        }
    }

    /**
     * 세금 수집
     */
    public CityMapResponse collectTax(String username, String cityName, String currentUsername) {
        if (!username.equals(currentUsername)) {
            throw new SecurityException("자신의 맵만 수정할 수 있습니다");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        CityMap cityMap;
        if (cityName != null && !cityName.isEmpty()) {
            cityMap = cityMapRepository.findByUserAndCityName(user, cityName).orElse(null);
            // "My City"로 요청했는데 없으면, 기본 도시 반환
            if (cityMap == null && "My City".equals(cityName)) {
                cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
            }
            if (cityMap == null) {
                throw new IllegalArgumentException("도시를 찾을 수 없습니다");
            }
        } else {
            cityMap = cityMapRepository.findFirstByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("맵을 찾을 수 없습니다"));
        }

        long earnings = cityStatsService.calculateOfflineEarnings(cityMap);
        cityMap.collectTax(earnings);
        cityMapRepository.save(cityMap);

        CityStatsResponse stats = cityStatsService.calculateStats(cityMap);

        return new CityMapResponse(
            cityMap, true, stats,
            stats.getCongestionMap(),
            stats.getTaxPerHour(),
            0L, 0L
        );
    }

    /**
     * 로그인 보상 수령
     */
    public CityMapResponse claimLoginReward(String username, String cityName, String currentUsername) {
        if (!username.equals(currentUsername)) {
            throw new SecurityException("자신의 맵만 수정할 수 있습니다");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        CityMap cityMap;
        if (cityName != null && !cityName.isEmpty()) {
            cityMap = cityMapRepository.findByUserAndCityName(user, cityName).orElse(null);
            // "My City"로 요청했는데 없으면, 기본 도시 반환
            if (cityMap == null && "My City".equals(cityName)) {
                cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
            }
            if (cityMap == null) {
                throw new IllegalArgumentException("도시를 찾을 수 없습니다");
            }
        } else {
            cityMap = cityMapRepository.findFirstByUser(user)
                    .orElseThrow(() -> new IllegalArgumentException("맵을 찾을 수 없습니다"));
        }

        long reward = cityStatsService.calculateLoginReward(cityMap.getConsecutiveLoginDays());
        cityMap.setMoney(cityMap.getMoney() + reward);
        cityMapRepository.save(cityMap);

        CityStatsResponse stats = cityStatsService.calculateStats(cityMap);

        return new CityMapResponse(
            cityMap, true, stats,
            stats.getCongestionMap(),
            stats.getTaxPerHour(),
            0L, 0L
        );
    }

    private void validateGrid(int[][] grid) {
        if (grid == null || grid.length != GRID_SIZE) {
            throw new IllegalArgumentException("그리드는 " + GRID_SIZE + "x" + GRID_SIZE + " 크기여야 합니다");
        }
        for (int[] row : grid) {
            if (row == null || row.length != GRID_SIZE) {
                throw new IllegalArgumentException("그리드는 " + GRID_SIZE + "x" + GRID_SIZE + " 크기여야 합니다");
            }
            for (int cell : row) {
                CellType type = CellType.fromCode(cell);
                if (type == null) {
                    throw new IllegalArgumentException("유효하지 않은 셀 값입니다: " + cell);
                }
            }
        }
        // 오른쪽 외곽 경계가 잠긴 상태인지 확인
        for (int i = 0; i < GRID_SIZE; i++) {
            if (grid[GRID_SIZE - 1][i] != 2) {
                throw new IllegalArgumentException("외곽 경계는 변경할 수 없습니다");
            }
        }
    }

    private String convertGridToJson(int[][] grid) {
        try {
            return objectMapper.writeValueAsString(grid);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("그리드 직렬화 실패", e);
        }
    }

    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * 사용자의 모든 도시 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CityMapResponse> getCitiesByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));

        List<CityMap> cities = cityMapRepository.findByUser(user);

        return cities.stream()
                .map(city -> new CityMapResponse(city, true))
                .collect(Collectors.toList());
    }

    /**
     * 새 도시 생성
     */
    @Transactional
    public CityMap createNewCity(String username, String cityName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 최대 도시 수 체크
        int cityCount = cityMapRepository.countByUser(user);
        if (cityCount >= MAX_CITIES_PER_USER) {
            throw new IllegalStateException("최대 " + MAX_CITIES_PER_USER + "개의 도시만 생성할 수 있습니다");
        }

        // 도시 이름 중복 체크
        if (cityMapRepository.existsByUserAndCityName(user, cityName)) {
            throw new IllegalArgumentException("이미 같은 이름의 도시가 있습니다: " + cityName);
        }

        // 슬러그 생성 (호환성 유지)
        String slug = generateSlug(cityName);
        if (slug.isEmpty()) {
            slug = "city-" + (cityCount + 1);
        }

        CityMap cityMap = CityMap.createDefault(user, cityName, slug);
        return cityMapRepository.save(cityMap);
    }

    /**
     * 도시 삭제
     */
    public void deleteCity(String username, String cityName, String currentUsername) {
        if (!username.equals(currentUsername)) {
            throw new SecurityException("자신의 도시만 삭제할 수 있습니다");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        CityMap cityMap = cityMapRepository.findByUserAndCityName(user, cityName).orElse(null);
        // "My City"로 요청했는데 없으면, 기본 도시 반환
        if (cityMap == null && "My City".equals(cityName)) {
            cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
        }
        if (cityMap == null) {
            throw new IllegalArgumentException("도시를 찾을 수 없습니다: " + cityName);
        }

        // 최소 1개의 도시는 유지
        int cityCount = cityMapRepository.countByUser(user);
        if (cityCount <= 1) {
            throw new IllegalStateException("최소 1개의 도시는 유지해야 합니다");
        }

        cityMapRepository.delete(cityMap);
    }

    /**
     * 도시 이름 변경
     */
    public CityMap renameCity(String username, String oldCityName, String newCityName, String currentUsername) {
        if (!username.equals(currentUsername)) {
            throw new SecurityException("자신의 도시만 수정할 수 있습니다");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 새 이름이 이미 존재하는지 확인
        if (cityMapRepository.existsByUserAndCityName(user, newCityName)) {
            throw new IllegalArgumentException("이미 같은 이름의 도시가 있습니다: " + newCityName);
        }

        CityMap cityMap = cityMapRepository.findByUserAndCityName(user, oldCityName).orElse(null);
        // "My City"로 요청했는데 없으면, 기본 도시 반환
        if (cityMap == null && "My City".equals(oldCityName)) {
            cityMap = cityMapRepository.findFirstByUser(user).orElse(null);
        }
        if (cityMap == null) {
            throw new IllegalArgumentException("도시를 찾을 수 없습니다: " + oldCityName);
        }

        cityMap.setCityName(newCityName);
        return cityMapRepository.save(cityMap);
    }

    /**
     * 슬러그 생성 (URL-friendly, ASCII만 허용)
     */
    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "city";
        }
        // 영문, 숫자, 하이픈만 허용 (한글 제외), 소문자로 변환
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")  // 영문, 숫자, 공백, 하이픈만
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // 슬러그가 비어있으면 (한글만 입력한 경우) 기본값 사용
        if (slug.isEmpty()) {
            return "city";
        }
        return slug.length() > 30 ? slug.substring(0, 30) : slug;
    }
}
