package com.example.portfolio.repository;

import com.example.portfolio.domain.MapTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MapTemplateRepository extends JpaRepository<MapTemplate, Long> {

    // 활성화된 템플릿 목록 조회
    List<MapTemplate> findByIsActiveTrueOrderByIsDefaultDescCreatedAtAsc();

    // 기본 템플릿 조회
    Optional<MapTemplate> findByIsDefaultTrue();

    // 이름으로 템플릿 조회
    Optional<MapTemplate> findByName(String name);

    // 이름 중복 체크
    boolean existsByName(String name);
}
