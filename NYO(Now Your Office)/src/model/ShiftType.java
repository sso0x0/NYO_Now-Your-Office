package model;

/**
 * 근무 유형을 나타내는 enum
 * 
 * 각 근무 유형은 급여 계산 시 적용할 배율(payRate)을 가진다.
 */
public enum ShiftType {

    // 일반 주간 근무
    DAY(1.0),

    // 야간 근무
    NIGHT(1.5),

    // 휴무
    OFF(0.0);

    // 급여 배율
    private final double payRate;

    /**
     * ShiftType 생성자
     * 
     * @param payRate 급여 배율
     */
    ShiftType(double payRate) {
        this.payRate = payRate;
    }

    /**
     * 급여 배율 반환
     * 
     * @return 급여 배율
     */
    public double getPayRate() {
        return payRate;
    }
}