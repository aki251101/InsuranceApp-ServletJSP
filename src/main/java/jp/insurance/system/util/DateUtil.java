package jp.insurance.system.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_FORMAT);
    }

    /**
     * 年度の開始日を取得（4月1日）
     * @param today 基準日
     * @return 年度開始日
     */
    public static LocalDate getFiscalYearStart(LocalDate today) {
        int year = today.getYear();
        if (today.getMonthValue() < 4) {
            year--;
        }
        return LocalDate.of(year, Month.APRIL, 1);
    }

    /**
     * 年度の終了日を取得（翌年3月31日）
     * @param today 基準日
     * @return 年度終了日
     */
    public static LocalDate getFiscalYearEnd(LocalDate today) {
        int year = today.getYear();
        if (today.getMonthValue() >= 4) {
            year++;
        }
        return LocalDate.of(year, Month.MARCH, 31);
    }

    /**
     * 月の開始日を取得
     * @param today 基準日
     * @return 月初日
     */
    public static LocalDate getMonthStart(LocalDate today) {
        return LocalDate.of(today.getYear(), today.getMonth(), 1);
    }

    /**
     * 月の終了日を取得
     * @param today 基準日
     * @return 月末日
     */
    public static LocalDate getMonthEnd(LocalDate today) {
        return today.withDayOfMonth(today.lengthOfMonth());
    }
}