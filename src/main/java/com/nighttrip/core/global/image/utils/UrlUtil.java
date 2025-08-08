package com.nighttrip.core.global.image.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UrlUtil {
    private UrlUtil() {}

    /** 경로 세그먼트 인코딩: 공백 '+' -> '%20' 치환 */
    public static String encodePathSegment(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** S3/NCP 원본 키(인코딩 금지) */
    public static String buildRawKey(String userId, String placeName, int index, String ext) {
        // 예: user-spot/42/스타필드 하남_1.webp
        return "user-spot/" + userId + "/" + placeName + "_" + index + "." + ext;
    }

    /** 조회용 Public URL(경로 세그먼트만 퍼센트 인코딩) */
    public static String buildPublicUrl(String bucket, String userId, String placeName, int index, String ext) {
        String fileName = placeName + "_" + index + "." + ext;
        String encoded = encodePathSegment(fileName);
        return "https://" + bucket + ".kr.object.ncloudstorage.com/user-spot/" + userId + "/" + encoded;
    }
}
