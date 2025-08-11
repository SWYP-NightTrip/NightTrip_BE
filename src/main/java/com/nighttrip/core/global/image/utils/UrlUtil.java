package com.nighttrip.core.global.image.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UrlUtil {

    private static final String PATH_HOST = "https://kr.object.ncloudstorage.com/";

    private UrlUtil() {}

    /** 경로 세그먼트 인코딩: 공백 '+' -> '%20' 치환 */
    public static String encodePathSegment(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** 전체 경로를 세그먼트 단위로 인코딩(슬래시는 유지) */
    public static String encodePathSegments(String path) {
        if (path == null || path.isEmpty()) return "";
        String[] parts = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            sb.append(encodePathSegment(parts[i]));
        }
        return sb.toString();
    }

    public static String urlDecode(String s) {
        if (s == null) return "";
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return s;
        }
    }

    public static String trimSlashes(String s) {
        if (s == null) return "";
        return s.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    public static boolean isAbsoluteUrl(String s) {
        if (s == null) return false;
        String l = s.toLowerCase();
        return l.startsWith("http://") || l.startsWith("https://");
    }

    /** S3/NCP 원본 키(인코딩 금지) */
    public static String buildRawKey(String userId, String placeName, int index, String ext) {
        return "user-spot/" + userId + "/" + placeName + "_" + index + "." + ext;
    }

    /** 조회용 Public URL(파일명만 퍼센트 인코딩) — path-style */
    public static String buildPublicUrl(String bucket, String userId, String placeName, int index, String ext) {
        String fileName = placeName + "_" + index + "." + ext;
        String encoded = encodePathSegment(fileName);
        return PATH_HOST + bucket + "/user-spot/" + userId + "/" + encoded;
    }

    /** 주어진 key를 path-style 공개 URL로 변환(세그먼트 인코딩 포함) */
    public static String buildPublicUrlFromKey(String bucket, String key) {
        String clean = trimSlashes(key);
        String encoded = encodePathSegments(clean);
        return PATH_HOST + bucket + "/" + encoded;
    }

    /**
     * 입력이 전체 URL이면 그대로 반환,
     * 키면 path-style 공개 URL로 변환(세그먼트 인코딩 포함)
     */
    public static String normalizeToPublicUrl(String bucket, String input) {
        if (isAbsoluteUrl(input)) return input;
        return buildPublicUrlFromKey(bucket, input);
    }

    /** path-style 혹은 virtual-hosted-style 공개 URL prefix 제거 → key로 변환(디코딩 포함) */
    public static String toKeyFromUrlOrKey(String bucket, String urlOrKey) {
        if (!isAbsoluteUrl(urlOrKey)) {
            return trimSlashes(urlOrKey);
        }
        String pathPrefix = PATH_HOST + bucket + "/";
        if (urlOrKey.startsWith(pathPrefix)) {
            String rest = urlOrKey.substring(pathPrefix.length());
            return trimSlashes(urlDecode(rest));
        }
        String vhost = "https://" + bucket + ".kr.object.ncloudstorage.com/";
        if (urlOrKey.startsWith(vhost)) {
            String rest = urlOrKey.substring(vhost.length());
            return trimSlashes(urlDecode(rest));
        }
        return trimSlashes(urlDecode(urlOrKey));
    }

    /** fileLocation + fileName을 안전하게 조합하여 key 생성(디코딩 & 슬래시 정리) */
    public static String joinKey(String fileLocation, String fileName) {
        String loc = trimSlashes(urlDecode(fileLocation));
        String name = urlDecode(fileName);
        name = name.replaceAll("^/+", "");
        return loc.isEmpty() ? name : (loc + "/" + name);
    }

    /** 경로에서 앞쪽 N개 세그먼트만 유지 (예: "user-spot/42/images" → N=2 → "user-spot/42") */
    public static String firstNSegments(String path, int n) {
        String[] parts = trimSlashes(path).split("/");
        if (parts.length <= n) return String.join("/", parts);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append('/');
            sb.append(parts[i]);
        }
        return sb.toString();
    }
}
