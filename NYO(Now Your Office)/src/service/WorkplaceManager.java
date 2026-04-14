package service;

import java.util.List;

import model.Shift;
import model.Workplace;

/**
 * 근무지(회사) 관련 순수 로직 모음입니다.
 *
 * 이 클래스에서는 실제 화면을 그리지 않고,
 * 근무지 생성/수정/삭제/검색/표시문자열 규칙만 처리합니다.
 *
 * 초보 팀원 수정 포인트:
 * - 근무지 목록에 보이는 문자열 형식 변경: buildWorkplaceDisplayText(...)
 * - 기본 근무지 생성 규칙 변경: getOrCreateDefaultWorkplace(...),
 *   syncDefaultWorkplaceFromUserInfo(...)
 */
public class WorkplaceManager {

    public void syncDefaultWorkplaceFromUserInfo(
            List<Workplace> workplaces,
            String userId,
            String companyName,
            int hourlyWage
    ) {
        validateWorkplaces(workplaces);

        String workplaceId = "WP-" + userId;
        String safeCompanyName = (companyName == null || companyName.isBlank())
                ? "미등록 회사"
                : companyName;

        Workplace updatedWorkplace = new Workplace(
                workplaceId,
                safeCompanyName,
                hourlyWage,
                8,
                8
        );

        if (workplaces.isEmpty()) {
            workplaces.add(updatedWorkplace);
        } else {
            workplaces.set(0, updatedWorkplace);
        }
    }

    public Workplace getOrCreateDefaultWorkplace(
            List<Workplace> workplaces,
            String userId,
            String companyName,
            int hourlyWage
    ) {
        validateWorkplaces(workplaces);

        if (workplaces.isEmpty()) {
            syncDefaultWorkplaceFromUserInfo(workplaces, userId, companyName, hourlyWage);
        }

        return workplaces.get(0);
    }

    public String buildWorkplaceDisplayText(Workplace workplace) {
        return String.format("%s (시급: %,d원)",
                workplace.getCompanyName(),
                workplace.getHourlyRate());
    }

    public Workplace findWorkplaceByDisplayText(List<Workplace> workplaces, String displayText) {
        if (workplaces == null) {
            return null;
        }

        for (Workplace workplace : workplaces) {
            if (buildWorkplaceDisplayText(workplace).equals(displayText)) {
                return workplace;
            }
        }

        return null;
    }

    public boolean isWorkplaceInUse(List<Shift> shifts, Workplace workplace) {
        if (shifts == null || workplace == null) {
            return false;
        }

        for (Shift shift : shifts) {
            if (shift.getWorkplace() != null &&
                    shift.getWorkplace().getId().equals(workplace.getId())) {
                return true;
            }
        }

        return false;
    }

    public Workplace addWorkplace(
            List<Workplace> workplaces,
            String companyName,
            long hourlyRate,
            int standardDayHours,
            int standardNightHours
    ) {
        validateWorkplaces(workplaces);
        validateWorkplaceInput(companyName, hourlyRate, standardDayHours, standardNightHours);

        String workplaceId = "WP-" + System.currentTimeMillis();

        Workplace newWorkplace = new Workplace(
                workplaceId,
                companyName.trim(),
                hourlyRate,
                standardDayHours,
                standardNightHours
        );

        workplaces.add(newWorkplace);
        return newWorkplace;
    }

    public Workplace updateWorkplace(
            List<Workplace> workplaces,
            List<Shift> shifts,
            Workplace oldWorkplace,
            String companyName,
            long hourlyRate,
            int standardDayHours,
            int standardNightHours
    ) {
        validateWorkplaces(workplaces);

        if (oldWorkplace == null) {
            throw new IllegalArgumentException("수정 대상 근무지가 없습니다.");
        }

        validateWorkplaceInput(companyName, hourlyRate, standardDayHours, standardNightHours);

        if (isWorkplaceInUse(shifts, oldWorkplace)) {
            throw new IllegalStateException("현재 일정에서 사용 중인 근무지는 수정할 수 없습니다.");
        }

        Workplace updatedWorkplace = new Workplace(
                oldWorkplace.getId(),
                companyName.trim(),
                hourlyRate,
                standardDayHours,
                standardNightHours
        );

        for (int i = 0; i < workplaces.size(); i++) {
            Workplace current = workplaces.get(i);
            if (current.getId().equals(oldWorkplace.getId())) {
                workplaces.set(i, updatedWorkplace);
                return updatedWorkplace;
            }
        }

        throw new IllegalArgumentException("수정 대상 근무지를 목록에서 찾을 수 없습니다.");
    }

    public void deleteWorkplace(
            List<Workplace> workplaces,
            List<Shift> shifts,
            Workplace targetWorkplace
    ) {
        validateWorkplaces(workplaces);

        if (targetWorkplace == null) {
            throw new IllegalArgumentException("삭제 대상 근무지가 없습니다.");
        }

        if (isWorkplaceInUse(shifts, targetWorkplace)) {
            throw new IllegalStateException("현재 일정에서 사용 중인 근무지는 삭제할 수 없습니다.");
        }

        workplaces.removeIf(w -> w.getId().equals(targetWorkplace.getId()));
    }

    private void validateWorkplaceInput(
            String companyName,
            long hourlyRate,
            int standardDayHours,
            int standardNightHours
    ) {
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("근무지 이름을 입력해주세요.");
        }
        if (hourlyRate <= 0) {
            throw new IllegalArgumentException("시급은 0보다 커야 합니다.");
        }
        if (standardDayHours <= 0 || standardNightHours <= 0) {
            throw new IllegalArgumentException("기본 근무시간은 0보다 커야 합니다.");
        }
    }

    private void validateWorkplaces(List<Workplace> workplaces) {
        if (workplaces == null) {
            throw new IllegalArgumentException("근무지 목록이 없습니다.");
        }
    }
}
