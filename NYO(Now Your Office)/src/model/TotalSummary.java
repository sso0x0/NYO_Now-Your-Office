package model;

/**
 * 전체 요약 통계 정보를 담는 DTO 클래스
 * 
 * 역할:
 * - 총 예상 급여
 * - 총 근무 시간
 * - 총 근무 횟수
 * 를 하나로 묶어서 전달
 */
public class TotalSummary {

    // 총 예상 급여
    private final long totalExpectedSalary;

    // 총 근무 시간
    private final double totalWorkHours;

    // 총 근무 횟수
    private final int totalShiftCount;

    /**
     * TotalSummary 생성자
     * 
     * @param totalExpectedSalary 총 예상 급여
     * @param totalWorkHours 총 근무 시간
     * @param totalShiftCount 총 근무 횟수
     */
    public TotalSummary(long totalExpectedSalary, double totalWorkHours, int totalShiftCount) {
        this.totalExpectedSalary = totalExpectedSalary;
        this.totalWorkHours = totalWorkHours;
        this.totalShiftCount = totalShiftCount;
    }

    /**
     * 총 예상 급여 반환
     * 
     * @return 총 예상 급여
     */
    public long getTotalExpectedSalary() {
        return totalExpectedSalary;
    }

    /**
     * 총 근무 시간 반환
     * 
     * @return 총 근무 시간
     */
    public double getTotalWorkHours() {
        return totalWorkHours;
    }

    /**
     * 총 근무 횟수 반환
     * 
     * @return 총 근무 횟수
     */
    public int getTotalShiftCount() {
        return totalShiftCount;
    }
}