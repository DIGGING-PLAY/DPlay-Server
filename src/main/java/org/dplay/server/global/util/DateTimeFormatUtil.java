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
}
