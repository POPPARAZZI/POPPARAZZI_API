package com.spoons.popparazzi.util;

import java.util.Locale;

public final class SearchKeywordNormalizer {

    private SearchKeywordNormalizer() {
    }

    /**
     * 사용자 입력 검색어를 정규화한다.
     * 1. null 이면 빈 문자열 반환
     * 2. 앞뒤 공백 제거
     * 3. 모든 공백 제거
     * 4. 영문은 소문자로 통일
     * 5. 한글/영문/숫자를 제외한 특수문자는 제거
     */
    public static String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }

        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String noSpaces = trimmed.replaceAll("\\s+", "");
        String lowerCased = noSpaces.toLowerCase(Locale.ROOT);

        return lowerCased.replaceAll("[^가-힣a-z0-9]", "");
    }

    /**
     * 검색 가능한 키워드인지 검증할 때 사용할 길이 기준 문자열.
     * 정규화 이후 길이를 기준으로 2~15자를 검사할 수 있다.
     */
    public static boolean isValidLength(String keyword) {
        String normalized = normalize(keyword);
        int length = normalized.length();
        return length >= 2 && length <= 15;
    }

    /**
     * SQL like 검색용 패턴 생성
     * 예: "시몬스" -> "%시몬스%"
     */
    public static String containsPattern(String keyword) {
        String normalized = normalize(keyword);
        return "%" + normalized + "%";
    }
}