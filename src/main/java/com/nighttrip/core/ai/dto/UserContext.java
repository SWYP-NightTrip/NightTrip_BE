package com.nighttrip.core.ai.dto;

public record UserContext(
        String cityId,
        String purpose,                  // "힐링","사진","미식","액티브" 등
        String budgetLevel,              // "저렴","보통","프리미엄" (식당/숙소 추정가 필터)
        String groupSize,               // 인원수
        String extras             // “루프탑”, “라이브”, “야시장”, “야간개장” 등 추가 희망
) {
}