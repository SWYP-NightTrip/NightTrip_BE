package com.nighttrip.core.global.dto;

import lombok.Getter;

@Getter
public  class RecommendedKeyword {
    private String id;
    private String name;

    public RecommendedKeyword(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
