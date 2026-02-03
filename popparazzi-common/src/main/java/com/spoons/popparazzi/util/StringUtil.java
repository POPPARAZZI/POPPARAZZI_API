package com.spoons.popparazzi.util;

/**
 * String 관련 Util
 * <p/>
 *
 * @author      sangwon-han
 * @version     0.0.1
 *
 * */
public final class StringUtil {

    /**
     * String 값에 대하여 null 혹은 blank 체크(공백 포함)를 통해서 true/false 값 리턴
     *
     * @param s String 값
     * @return boolean
     */
    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * String 값에 대하여 null 혹은 empty 체크(공백 미포함)를 통해서 true/false 값 리턴
     *
     * @param s String 값
     * @return boolean
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}



