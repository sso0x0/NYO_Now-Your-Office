package test;

import model.Shift;
import model.ShiftType;
import model.StatisticsPeriod;
import model.TotalSummary;
import model.Workplace;
import service.HourlySalaryStrategy;
import service.OffSalaryStrategy;
import service.SalaryEngine;
import service.SalaryStrategy;
import service.StatisticsService;
import service.StorageService;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendFinalTest {

	public static void main(String[] args) {
		// ==========================================
		// 1. 전략 맵 구성
		// DAY, NIGHT는 시급 기반 계산
		// OFF는 0원 처리
		// ==========================================
		Map<ShiftType, SalaryStrategy> strategyMap = new HashMap<>();
		strategyMap.put(ShiftType.DAY, new HourlySalaryStrategy());
		strategyMap.put(ShiftType.NIGHT, new HourlySalaryStrategy());
		strategyMap.put(ShiftType.OFF, new OffSalaryStrategy());

		SalaryEngine salaryEngine = new SalaryEngine(strategyMap);
		StatisticsService statisticsService = new StatisticsService(salaryEngine);
		StorageService storageService = new StorageService();

		// ==========================================
		// 2. 더미 데이터 생성
		// 쉼표 포함 회사명, 큰따옴표 포함 회사명 테스트
		// ==========================================
		List<Workplace> workplaces = new ArrayList<>();
		workplaces.add(new Workplace("W1", "Samsung, Electronics", 12000L, 8, 8));
		workplaces.add(new Workplace("W2", "Test \"Store\"", 15000L, 8, 6));

		List<Shift> shifts = new ArrayList<>();
		shifts.add(new Shift("S1", workplaces.get(0), LocalDateTime.of(2026, 4, 10, 9, 0),
				LocalDateTime.of(2026, 4, 10, 18, 0), ShiftType.DAY));

		shifts.add(new Shift("S2", workplaces.get(1), LocalDateTime.of(2026, 4, 11, 22, 0),
				LocalDateTime.of(2026, 4, 12, 6, 0), ShiftType.NIGHT));

		shifts.add(new Shift("S3", workplaces.get(0), LocalDateTime.of(2026, 4, 13, 9, 0),
				LocalDateTime.of(2026, 4, 13, 17, 30), ShiftType.DAY));

		// ==========================================
		// 3. 저장 전 급여 계산
		// ==========================================
		TotalSummary beforeSummary = statisticsService.calculateTotalSummary(shifts);

		// ==========================================
		// 4. CSV 저장
		// ==========================================
		Path workplacePath = Path.of("workplaces.csv");
		Path shiftPath = Path.of("shifts.csv");

		storageService.saveWorkplaces(workplaces, workplacePath);
		storageService.saveShifts(shifts, shiftPath);

		// ==========================================
		// 5. CSV 다시 로드
		// ==========================================
		List<Workplace> loadedWorkplaces = storageService.loadWorkplaces(workplacePath);
		List<Shift> loadedShifts = storageService.loadShifts(shiftPath, loadedWorkplaces);

		// ==========================================
		// 6. 로드 후 급여 계산
		// ==========================================
		TotalSummary afterSummary = statisticsService.calculateTotalSummary(loadedShifts);

		// ==========================================
		// 7. 검증 결과 출력
		// ==========================================
		System.out.println("===== 저장 전/후 급여 비교 =====");
		System.out.println("저장 전 총 급여: " + beforeSummary.getTotalExpectedSalary());
		System.out.println("로드 후 총 급여: " + afterSummary.getTotalExpectedSalary());
		System.out.println();

		System.out.println("===== 저장 전/후 근무시간 비교 =====");
		System.out.println("저장 전 총 시간: " + beforeSummary.getTotalWorkHours());
		System.out.println("로드 후 총 시간: " + afterSummary.getTotalWorkHours());
		System.out.println();

		System.out.println("===== 회사명 로드 확인 =====");
		for (Workplace workplace : loadedWorkplaces) {
			System.out.println(
					workplace.getId() + " / " + workplace.getCompanyName() + " / " + workplace.getHourlyRate());
		}
		System.out.println();

		System.out.println("===== 기간별 통계 확인 (MONTH) =====");
		Map<String, Long> monthStats = statisticsService.getSalaryDataByPeriod(loadedShifts, StatisticsPeriod.MONTH);

		for (Map.Entry<String, Long> entry : monthStats.entrySet()) {
			System.out.println(entry.getKey() + " -> " + entry.getValue());
		}
		System.out.println();

		// ==========================================
		// 8. 최종 판정
		// ==========================================
		boolean sameSalary = beforeSummary.getTotalExpectedSalary() == afterSummary.getTotalExpectedSalary();
		boolean sameShiftCount = beforeSummary.getTotalShiftCount() == afterSummary.getTotalShiftCount();
		boolean sameWorkplaceCount = workplaces.size() == loadedWorkplaces.size();

		System.out.println("===== 최종 판정 =====");
		System.out.println("급여 동일 여부: " + sameSalary);
		System.out.println("근무 개수 동일 여부: " + sameShiftCount);
		System.out.println("근무지 개수 동일 여부: " + sameWorkplaceCount);

		if (sameSalary && sameShiftCount && sameWorkplaceCount) {
			System.out.println("테스트 성공: CSV 저장/로드 및 통계 계산이 정상 동작합니다.");
		} else {
			System.out.println("테스트 실패: 저장/로드 또는 계산 로직을 다시 확인하세요.");
		}
	}
}