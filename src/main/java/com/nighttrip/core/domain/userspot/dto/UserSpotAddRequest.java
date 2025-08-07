package com.nighttrip.core.domain.userspot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserSpotAddRequest(
        @NotBlank(message = "장소 이름은 필수입니다.")
        @Size(max = 100, message = "장소 이름은 최대 100자까지 입력 가능합니다.")
        String placeName,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 200)
        String placeAddress,

        @Size(max = 500, message = "설명은 최대 500자까지 입력 가능합니다.")
        String placeExplain,

        @NotBlank(message = "썸네일은 비어 있을 수 없습니다.")
        @Size(max = 300)
        String thumbnailUrl,

        List<String> imageUrl,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        List<String> details
) {
}
