package com.nighttrip.core.global.util;

public class LocationFormatter {

    public static String format(String fullLocation) {

        if (fullLocation == null || fullLocation.isBlank()) {
            return fullLocation;
        }

        if (fullLocation.startsWith("세종특별자치시")) {
            return "세종시";
        }

        String tempLocation = fullLocation;

        if (tempLocation.startsWith("서울")) {
            tempLocation = tempLocation.replace("특별시", "시");
        } else if (tempLocation.contains("광역시")) {
            tempLocation = tempLocation.replace("광역시", "시");
        } else if (tempLocation.startsWith("제주") || tempLocation.startsWith("강원")) {
            tempLocation = tempLocation.replace("특별자치도", "도");
        } else if (tempLocation.contains("특별자치도")) {
            tempLocation = tempLocation.replace("특별자치도", "");
        }

        String[] parts = tempLocation.split(" ");

        if (parts.length >= 3) {
            return parts[0] + " " + parts[1];
        }

        return tempLocation;
    }

    public static String formatForSearch(String fullLocation) {
        if (fullLocation == null || fullLocation.isBlank()) {
            return fullLocation;
        }

        if ("세종특별자치시 세종특별자치시".equals(fullLocation)) {
            return "세종특별자치시";
        }

        String[] parts = fullLocation.split(" ");

        if (parts.length >= 3) {
            return parts[0] + " " + parts[1];
        }

        return fullLocation;
    }
}
