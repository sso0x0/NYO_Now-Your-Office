package service;

import java.util.Map;

import model.Shift;
import model.ShiftType;

/**
 * 근무 유형에 따라 적절한 급여 계산 전략을 선택하는 엔진 클래스
 *
 * 역할:
 * - ShiftType에 맞는 SalaryStrategy를 찾는다.
 * - 선택된 전략으로 급여 계산을 위임한다.
 */
public class SalaryEngine {

    // 근무 유형별 전략을 저장하는 맵
    private final Map<ShiftType, SalaryStrategy> strategyMap;

    /**
     * SalaryEngine 생성자
     *
     * @param strategyMap ShiftType별 전략 맵
     */
    public SalaryEngine(Map<ShiftType, SalaryStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    /**
     * Shift 하나에 대한 급여를 계산한다.
     *
     * @param shift 근무 정보
     * @return 계산된 급여
     */
    public long calculateSalary(Shift shift) {
        SalaryStrategy strategy = strategyMap.get(shift.getShiftType());

        if (strategy == null) {
            throw new IllegalArgumentException(
                "해당 근무 유형에 대한 급여 계산 전략이 없습니다: " + shift.getShiftType()
            );
        }

        return strategy.calculate(shift);
    }
}