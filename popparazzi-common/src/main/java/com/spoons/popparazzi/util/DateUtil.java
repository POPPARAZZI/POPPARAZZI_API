package com.spoons.popparazzi.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Date 관련 Util
 * <p/>
 *
 * @author      sangwon-han
 * @version     0.0.1
 *
 * */
public final class DateUtil {

    /** date formatter - yyyy-MM ex) 2023-27 */
    public static final DateTimeFormatter YEAR_MONTH =  DateTimeFormatter.ofPattern("yyyy-MM");
    /** date formatter - yyyyMMdd ex) 20230727 */
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** time formatter - HHmmss ex) 151101 */
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmss");
    /** datetime formatter - yyyyMMddHHmmss ex) 20230727151101 */
    public static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    /** date formatter with dash - yyyy-MM-dd ex) 2023-07-27 */
    public static final DateTimeFormatter DATE_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** time formatter with colon - HH:mm:ss ex) 15:11:01 */
    public static final DateTimeFormatter TIME_COLON = DateTimeFormatter.ofPattern("HH:mm:ss");
    /** datetime formatter with default pattern - yyyy-MM-dd HH:mm:ss ex) 2023-07-27 15:11:01 */
    public static final DateTimeFormatter DATETIME_DEFAULT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 오늘 시작 시간 (00:00:00)
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    // 오늘 끝 시간 (23:59:59.999999999)
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    // 6일 전의 00:00:00 , 14일 전의 00:00:00
    public static LocalDateTime getStartOfWeek(LocalDate date , int day) {
        return date.minusDays(day).atStartOfDay();
    }

    // 이번 달의 첫날
    public static LocalDateTime getStartOfMonth(LocalDate date) {
        return date.withDayOfMonth(1).atStartOfDay();
    }

    // 이번 달의 막날
    public static LocalDateTime getEndOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX);
    }

    // 11개월 전 첫날 00:00:00
    public static LocalDateTime getStartOf12Months(LocalDate date) {
        return date.minusMonths(11).withDayOfMonth(1).atStartOfDay();
    }

    public static List<String> generateLast12Months() {
        return IntStream.rangeClosed(0, 11)
                .mapToObj(i -> LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .sorted()
                .toList();
    }

    public static List<String> generateLastNDays(int searchFirst,int searchLast) {
        return IntStream.rangeClosed(searchFirst, searchLast)
                .mapToObj(i -> LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .sorted()
                .toList();
    }

    public static String formatDateRange(LocalDateTime start, LocalDateTime end) {
        return start.format(DateTimeFormatter.ISO_LOCAL_DATE) + " ~ " + end.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String formatMonth(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }


}
