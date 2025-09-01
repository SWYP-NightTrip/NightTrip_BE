package com.nighttrip.core.ai.dto;

public record UserContext(
        String tripDuration,             // "당일","1박2일","2박3일","3박4일","4~5일","6~7일"
        String travelTime,               // "오전","오후","저녁","심야" 등(선택)
        String cityId,
        String purpose,                  // "힐링","사진","미식","액티브" 등
        String budgetLevel,              // "저렴","보통","프리미엄" (식당/숙소 추정가 필터)
        String groupSize,               // 인원수
        String extras             // “루프탑”, “라이브”, “야시장”, “야간개장” 등 추가 희망
) {
}