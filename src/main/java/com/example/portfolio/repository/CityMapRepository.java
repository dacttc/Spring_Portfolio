package com.example.portfolio.repository;

import com.example.portfolio.domain.CityMap;
import com.example.portfolio.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityMapRepository extends JpaRepository<CityMap, Long> {
    // 기존 메서드 (호환성 - 첫 번째 도시 반환)
    Optional<CityMap> findFirstByUser(User user);
    Optional<CityMap> findFirstByUserUsername(String username);
    boolean existsByUser(User user);

    // 멀티 도시 지원
    List<CityMap> findByUser(User user);
    List<CityMap> findByUserUsername(String username);
    Optional<CityMap> findByUserAndSlug(User user, String slug);
    Optional<CityMap> findByUserUsernameAndSlug(String username, String slug);
    boolean existsByUserAndSlug(User user, String slug);
    int countByUser(User user);

    // 도시 이름으로 조회
    Optional<CityMap> findByUserAndCityName(User user, String cityName);
    Optional<CityMap> findByUserUsernameAndCityName(String username, String cityName);
    boolean existsByUserAndCityName(User user, String cityName);
}
