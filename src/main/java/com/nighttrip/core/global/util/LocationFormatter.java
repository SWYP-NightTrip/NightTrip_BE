package com.nighttrip.core.global.util;

public class LocationFormatter {

    public static String format(String fullLocation) {
        // null이나 빈 문자열일 경우, 그대로 반환하여 오류를 방지합니다.
        if (fullLocation == null || fullLocation.isBlank()) {
            return fullLocation;
        }

        // 가장 특수한 케이스인 '세종특별자치시'를 먼저 처리합니다.
        if (fullLocation.startsWith("세종특별자치시")) {
            return "세종시";
        }

        String tempLocation = fullLocation;

        // '특별자치도', '광역시', '특별시'를 변환합니다.
        if (tempLocation.startsWith("서울")) {
            tempLocation = tempLocation.replace("특별시", "시");
        } else if (tempLocation.contains("광역시")) {
            tempLocation = tempLocation.replace("광역시", "시");
        } else if (tempLocation.startsWith("제주") || tempLocation.startsWith("강원")) {
            // 제주, 강원은 '도'를 붙입니다.
            tempLocation = tempLocation.replace("특별자치도", "도");
        } else if (tempLocation.contains("특별자치도")) {
            // 그 외(전북 등)는 '특별자치도'를 제거합니다.
            tempLocation = tempLocation.replace("특별자치도", "");
        }

        // 공백을 기준으로 문자열을 나눕니다.
        String[] parts = tempLocation.split(" ");

        // 분리된 부분이 3개 이상일 경우 (예: "경기도 수원시 장안구"), 앞의 두 부분만 사용합니다.
        if (parts.length >= 3) {
            return parts[0] + " " + parts[1];
        }

        // 그 외의 경우는 처리된 문자열 전체를 반환합니다.
        return tempLocation;
    }
}
