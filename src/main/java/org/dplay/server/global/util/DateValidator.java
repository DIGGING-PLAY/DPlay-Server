package org.dplay.server.global.util;

public final class DateValidator {

    private DateValidator() {
    }

    /**
     * 월이 유효한 범위(1~12)인지 검증
     *
     * @param month 검증할 월
     * @return 유효하면 true
     */
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    /**
     * 연도가 유효한 범위인지 검증
     *
     * @param year 검증할 연도
     * @return 유효하면 true
     */
    public static boolean isValidYear(int year) {
        return year >= 2000 && year <= 9999;
    }
}
