package org.dplay.server.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateTimeFormatUtil {

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String formatDate(LocalDate date) {
        return formatDate(date, DATE);
    }

    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(formatter, "formatter");
        return date.format(formatter);
    }

    /**
     * 날짜의 일(day)을 "X일" 형식으로 포맷팅
     * @param date 포맷팅할 날짜
     * @return "1일", "2일" 등의 형식 문자열
     */
    public static String formatDayOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return date.getDayOfMonth() + "일";
    }
}
