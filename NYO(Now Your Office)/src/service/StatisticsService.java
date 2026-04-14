package service;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import model.Shift;
import model.StatisticsPeriod;
import model.TotalSummary;

/**
 * 근무 기록 리스트를 기반으로 기간별 급여 통계와 전체 요약 정보를 생성하는 서비스 클래스
 *
 * 역할: 1. 기간별 급여 집계 2. 전체 급여/근무시간/근무횟수 요약
 *
 * UI 팀은 이 클래스를 통계 진입점으로 사용하면 됨
 */
public class StatisticsService {

	// Shift 하나당 급여를 계산하는 엔진
	private final SalaryEngine salaryEngine;

	/**
	 * StatisticsService 생성자
	 *
	 * @param salaryEngine 급여 계산 엔진
	 */
	public StatisticsService(SalaryEngine salaryEngine) {
		this.salaryEngine = salaryEngine;
	}

	public TotalSummary calculateTotalSummary(List<Shift> shifts) {
		long totalExpectedSalary = 0L;
		double totalWorkHours = 0.0;
		int totalShiftCount = 0;

		// 방어 코드:
		// null 또는 비어 있는 리스트가 들어오면 0값 요약을 반환
		if (shifts == null || shifts.isEmpty()) {
			return new TotalSummary(0L, 0.0, 0);
		}

		for (Shift shift : shifts) {
			totalExpectedSalary += salaryEngine.calculateSalary(shift);
			totalWorkHours += shift.getWorkedHours();
			totalShiftCount++;
		}

		return new TotalSummary(totalExpectedSalary, totalWorkHours, totalShiftCount);
	}

	/**
	 * 기간별 급여 데이터 반환
	 *
	 * 예: DAY -> 2026-04-09 WEEK -> 2026-W15 MONTH -> 2026-04
	 *
	 * @param shifts 근무 기록 리스트
	 * @param period 통계 기준 기간
	 * @return 기간별 급여 합계
	 */
	public Map<String, Long> getSalaryDataByPeriod(List<Shift> shifts, StatisticsPeriod period) {
	    Map<String, Long> periodStats = new TreeMap<>();

	    // 방어 코드:
	    // null 또는 비어 있는 리스트면 빈 Map 반환
	    if (shifts == null || shifts.isEmpty()) {
	        return periodStats;
	    }

	    for (Shift shift : shifts) {
	        String key = generateKey(shift.getStartTime(), period);
	        long pay = salaryEngine.calculateSalary(shift);
	        periodStats.put(key, periodStats.getOrDefault(key, 0L) + pay);
	    }

	    return periodStats;
	}

	/**
	 * 날짜/시간과 통계 기간을 받아 통계용 key 문자열을 생성
	 *
	 * @param dateTime 기준 날짜/시간
	 * @param period   통계 기간
	 * @return 통계 key 문자열
	 */
	private String generateKey(LocalDateTime dateTime, StatisticsPeriod period) {
		switch (period) {
		case DAY:
			// 예: 2026-04-09
			return dateTime.toLocalDate().toString();

		case WEEK:
			// 예: 2026-W15
			int week = dateTime.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
			int year = dateTime.get(WeekFields.of(Locale.getDefault()).weekBasedYear());
			return year + "-W" + week;

		case MONTH:
			// 예: 2026-04
			return String.format("%d-%02d", dateTime.getYear(), dateTime.getMonthValue());

		default:
			throw new IllegalArgumentException("지원하지 않는 기간 형식입니다: " + period);
		}
	}
}