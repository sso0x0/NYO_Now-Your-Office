package service;

import model.Shift;

/**
 * 급여 계산 전략 인터페이스
 * 
 * 근무 유형별로 계산 방식이 달라질 수 있으므로
 * 전략 패턴을 적용하기 위한 공통 인터페이스이다.
 */
public interface SalaryStrategy {

    /**
     * Shift 하나에 대한 급여를 계산한다.
     * 
     * @param shift 근무 정보
     * @return 계산된 급여
     */
    long calculate(Shift shift);
}