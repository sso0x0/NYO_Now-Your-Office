package model;

import java.io.Serializable;

/**
 * 회사(근무지) 정보를 저장하는 클래스
 * 
 * 역할:
 * - 회사별 고유 ID 보관
 * - 회사명 보관
 * - 회사별 시급 보관
 * - 기본 주간/야간 근무시간 기준 보관
 */
public class Workplace implements Serializable{
	 private static final long serialVersionUID = 1L;
    // 회사 고유 ID
    private final String id;

    // 회사명
    private final String companyName;

    // 시급
    private final long hourlyRate;

    // 기본 주간 근무 시간
    private final int standardDayHours;

    // 기본 야간 근무 시간
    private final int standardNightHours;

    /**
     * Workplace 생성자
     * 
     * @param id 회사 ID
     * @param companyName 회사명
     * @param hourlyRate 시급
     * @param standardDayHours 기본 주간 근무 시간
     * @param standardNightHours 기본 야간 근무 시간
     */
    
    
    
    public Workplace (String id, String companyName, long hourlyRate,
                     int standardDayHours, int standardNightHours) {
        this.id = id;
        this.companyName = companyName;
        this.hourlyRate = hourlyRate;
        this.standardDayHours = standardDayHours;
        this.standardNightHours = standardNightHours;
    }

    /**
     * 회사 ID 반환
     * 
     * @return 회사 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 회사명 반환
     * 
     * @return 회사명
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * 시급 반환
     * 
     * @return 시급
     */
    public long getHourlyRate() {
        return hourlyRate;
    }

    /**
     * 기본 주간 근무 시간 반환
     * 
     * @return 기본 주간 근무 시간
     */
    public int getStandardDayHours() {
        return standardDayHours;
    }

    /**
     * 기본 야간 근무 시간 반환
     * 
     * @return 기본 야간 근무 시간
     */
    public int getStandardNightHours() {
        return standardNightHours;
    }
}