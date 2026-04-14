package service;

import model.Shift;

/**
 * 일반적인 시급 계산 전략 클래스
 * 
 * DAY, NIGHT처럼 실제 근무 시간이 존재하는 Shift에 대해
 * 시급 × 근무시간 × 배율 방식으로 급여를 계산한다.
 */
public class HourlySalaryStrategy implements SalaryStrategy {

    /**
     * 급여 계산 메서드
     * 
     * 계산식:
     * 근무시간 × 시급 × 근무유형 배율
     * 
     * @param shift 근무 정보
     * @return 계산된 급여
     */
    @Override
    public long calculate(Shift shift) {
        // 실제 근무 시간
        double workedHours = shift.getWorkedHours();

        // 회사별 시급
        long hourlyRate = shift.getWorkplace().getHourlyRate();

        // 근무 유형별 배율
        double payRate = shift.getShiftType().getPayRate();

        // 계산 결과를 반올림하여 long으로 반환
        return Math.round(workedHours * hourlyRate * payRate);
    }
}