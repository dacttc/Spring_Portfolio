package com.example.portfolio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityMapUpdateRequest {
    @NotNull
    private int[][] grid;

    @NotNull
    @Min(0)
    private Long money;

    private Object buildings;  // 건물 데이터 (JSON array)

    private Object cameraState;  // 카메라 위치/타겟 (JSON object)

    private Object gameState;  // 게임 시간/날짜 (JSON object)
}
