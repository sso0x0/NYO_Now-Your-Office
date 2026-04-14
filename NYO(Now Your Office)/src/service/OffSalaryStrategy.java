package service;

import model.Shift;

/**
 * 휴무(OFF) 전용 급여 계산 전략 클래스
 * 
 * 휴무는 실제 급여가 없으므로 항상 0을 반환한다.
 */
public class OffSalaryStrategy implements SalaryStrategy {

    /**
     * OFF 근무 급여 계산
     * 
     * @param shift 근무 정보
     * @return 항상 0
     */
    @Override
    public long calculate(Shift shift) {
        return 0L;
    }
}