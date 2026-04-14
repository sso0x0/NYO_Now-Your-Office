package service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import model.Shift;
import model.ShiftType;
import model.Workplace;

/**
 * Shift(일정) 관련 순수 로직 모음입니다.
 *
 * 이 클래스는 "화면과 무관한 규칙"을 담는 곳입니다.
 * 예:
 * - UI 문자열 <-> ShiftType 변환
 * - 날짜 기준 일정 찾기
 * - 근무지 필터링
 *
 * 초보 팀원이 규칙만 바꾸고 싶을 때 먼저 보면 좋은 파일입니다.
 */
public class ShiftManager {

    public ShiftType convertUiShiftTypeToModel(String uiShiftType) {
        if ("주간근무".equals(uiShiftType)) {
            return ShiftType.DAY;
        } else if ("야간근무".equals(uiShiftType)) {
            return ShiftType.NIGHT;
        } else if ("휴무".equals(uiShiftType)) {
            return ShiftType.OFF;
        }

        throw new IllegalArgumentException("지원하지 않는 UI 근무 유형입니다: " + uiShiftType);
    }

    public String convertModelShiftTypeToUi(ShiftType shiftType) {
        if (shiftType == ShiftType.DAY) {
            return "주간근무";
        } else if (shiftType == ShiftType.NIGHT) {
            return "야간근무";
        } else if (shiftType == ShiftType.OFF) {
            return "휴무";
        }

        throw new IllegalArgumentException("지원하지 않는 ShiftType입니다: " + shiftType);
    }

    public Shift createShiftFromUiInput(String shiftId, Workplace workplace, String dateString,
            String startTimeString, String endTimeString, String uiShiftType) {

        LocalDate date = LocalDate.parse(dateString);
        LocalTime startTime = LocalTime.parse(startTimeString);
        LocalTime endTime = LocalTime.parse(endTimeString);

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        ShiftType shiftType = convertUiShiftTypeToModel(uiShiftType);

        if (!endDateTime.isAfter(startDateTime)) {
            endDateTime = endDateTime.plusDays(1);
        }

        return new Shift(shiftId, workplace, startDateTime, endDateTime, shiftType);
    }

    public Shift findShiftById(List<Shift> shifts, String shiftId) {
        if (shifts == null || shiftId == null) {
            return null;
        }

        for (Shift shift : shifts) {
            if (shift.getId().equals(shiftId)) {
                return shift;
            }
        }

        return null;
    }

    public Shift findShiftByDate(List<Shift> shifts, String dateString) {
        if (shifts == null || dateString == null) {
            return null;
        }

        for (Shift shift : shifts) {
            String shiftDate = shift.getStartTime().toLocalDate().toString();
            if (shiftDate.equals(dateString)) {
                return shift;
            }
        }

        return null;
    }

    public List<Shift> getFilteredShifts(List<Shift> shifts, String selectedWorkplaceFilterId) {
        List<Shift> filteredShifts = new ArrayList<>();
        if (shifts == null) {
            return filteredShifts;
        }

        for (Shift shift : shifts) {
            if (selectedWorkplaceFilterId == null) {
                filteredShifts.add(shift);
            } else if (shift.getWorkplace() != null
                    && selectedWorkplaceFilterId.equals(shift.getWorkplace().getId())) {
                filteredShifts.add(shift);
            }
        }

        return filteredShifts;
    }
}
