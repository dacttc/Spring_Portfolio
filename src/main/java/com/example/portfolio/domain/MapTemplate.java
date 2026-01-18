package com.example.portfolio.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "map_templates")
public class MapTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;  // 템플릿 이름 (예: "해변 도시", "산악 지형")

    @Column(length = 255)
    private String description;  // 템플릿 설명

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String gridData;  // 맵 그리드 데이터 (JSON)

    @Column(length = 255)
    private String thumbnailUrl;  // 미리보기 이미지 URL (선택)

    @Column(nullable = false)
    private Boolean isDefault = false;  // 기본 템플릿 여부

    @Column(nullable = false)
    private Boolean isActive = true;  // 활성화 여부

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MapTemplate(String name, String description, String gridData, String thumbnailUrl, Boolean isDefault) {
        this.name = name;
        this.description = description;
        this.gridData = gridData;
        this.thumbnailUrl = thumbnailUrl;
        this.isDefault = isDefault != null ? isDefault : false;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
