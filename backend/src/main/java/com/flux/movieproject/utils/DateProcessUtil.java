package com.flux.movieproject.utils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 日期處理工具類
 */
public class DateProcessUtil {

	public static List<LocalDate> getMonthDates(Integer year, Integer month) {
		// 1. 創建年月資訊物件
		YearMonth yearMonth = YearMonth.of(year, month);
		// 2. 取得該月份總天數
		int daysInMonth = yearMonth.lengthOfMonth();

		List<LocalDate> allDatesInMonth = new ArrayList<>();

		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate date = LocalDate.of(year, month, day);
			allDatesInMonth.add(date);
		}

		return allDatesInMonth;
	}
}
