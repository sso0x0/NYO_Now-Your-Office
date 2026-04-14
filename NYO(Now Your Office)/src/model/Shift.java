package model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 하나의 근무 기록(Shift)을 나타내는 클래스
 * 
 * 역할:
 * - 어떤 회사에서 근무했는지 저장
 * - 근무 시작/종료 시각 저장
 * - 근무 유형 저장
 * - 근무 시간 계산 제공
 * 
 * 현재 프로젝트에서는 Employee 개념보다
 * Workplace 중심 구조가 우선이므로 Employee 필드는 포함하지 않음
 */
public class Shift implements Serializable{

	 private static final long serialVersionUID = 1L;
	
	
    // 근무 고유 식별자
    private final String id; // 근무 고유 식별자

    // 소속 근무지(회사)
    private final Workplace workplace;

    // 근무 시작 시간
    private final LocalDateTime startTime;

    // 근무 종료 시간
    private final LocalDateTime endTime;

    // 근무 유형
    private final ShiftType shiftType;

    /**
     * Shift 객체 생성자
     * 
     * @param id 근무 ID
     * @param workplace 근무지
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param shiftType 근무 유형
     */
    public Shift(String id, Workplace workplace, LocalDateTime startTime,
                 LocalDateTime endTime, ShiftType shiftType) {

        // null 방지
        this.id = Objects.requireNonNull(id, "근무 ID는 필수입니다.");
        this.workplace = Objects.requireNonNull(workplace, "근무지 정보는 필수입니다.");
        this.startTime = Objects.requireNonNull(startTime, "시작 시간은 필수입니다.");
        this.endTime = Objects.requireNonNull(endTime, "종료 시간은 필수입니다.");
        this.shiftType = Objects.requireNonNull(shiftType, "근무 유형은 필수입니다.");

        // 시간 순서 검증
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
    }

    /**
     * 근무 ID 반환
     * 
     * @return 근무 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 근무지 반환
     * 
     * @return Workplace 객체
     */
    public Workplace getWorkplace() {
        return workplace;
    }

    /**
     * 시작 시간 반환
     * 
     * @return 시작 시간
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * 종료 시간 반환
     * 
     * @return 종료 시간
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public ShiftType getShiftType() { // 근무 유형 반환
        return shiftType;
    }

    // 실제 근무 시간을 시간 단위(double)로 반환 (ex. 09:00 ~ 17:30 -> 8.5)
    public double getWorkedHours() {
        return Duration.between(startTime, endTime).toMinutes() / 60.0;
    }
}