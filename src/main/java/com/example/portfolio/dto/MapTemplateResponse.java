package com.example.portfolio.dto;

import com.example.portfolio.domain.MapTemplate;
import lombok.Getter;

@Getter
public class MapTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private String gridData;
    private String thumbnailUrl;
    private Boolean isDefault;

    public MapTemplateResponse(MapTemplate template) {
        this.id = template.getId();
        this.name = template.getName();
        this.description = template.getDescription();
        this.gridData = template.getGridData();
        this.thumbnailUrl = template.getThumbnailUrl();
        this.isDefault = template.getIsDefault();
    }
}
