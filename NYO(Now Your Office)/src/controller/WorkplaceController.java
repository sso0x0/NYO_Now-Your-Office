package controller;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import model.Shift;
import model.Workplace;
import service.WorkplaceManager;
import ui.AddWorkplaceDialog;
import ui.DeleteWorkplaceDialog;
import ui.EditWorkplaceDialog;
import ui.SelectWorkplaceForEditDialog;

/**
 * 근무지 기능 전용 컨트롤러
 * 
 * 역할:
 * - 근무지 추가 / 수정 / 삭제 흐름을 담당한다.
 * - 다이얼로그 호출을 담당한다.
 * - 입력 검증과 실제 WorkplaceManager 호출을 담당한다.
 * - 작업 완료 후 저장 및 화면 갱신 콜백을 실행한다.
 * 
 * 주의:
 * - 실제 화면 조립은 WorkerCalendarAppFinal이 담당한다.
 * - 이 컨트롤러는 근무지 기능만 담당한다.
 */
public class WorkplaceController {

    /**
     * 로그인 사용자 데이터에 접근하기 위한 인터페이스
     * 
     * 이유:
     * - WorkerCalendarAppFinal 전체를 직접 참조하지 않기 위함
     * - 필요한 데이터만 안전하게 전달받기 위함
     */
    public interface WorkplaceDataProvider {
        WorkplaceUserContext getUserContext();
    }

    /**
     * 근무지 작업에 필요한 사용자 컨텍스트 데이터
     */
    public static class WorkplaceUserContext {
        private final String userId;
        private final List<Workplace> workplaces;
        private final List<Shift> shifts;

        public WorkplaceUserContext(String userId, List<Workplace> workplaces, List<Shift> shifts) {
            this.userId = userId;
            this.workplaces = workplaces;
            this.shifts = shifts;
        }

        public String getUserId() {
            return userId;
        }

        public List<Workplace> getWorkplaces() {
            return workplaces;
        }

        public List<Shift> getShifts() {
            return shifts;
        }
    }

    private final Frame parentFrame;
    private final WorkplaceManager workplaceManager;
    private final WorkplaceDataProvider dataProvider;

    /**
     * 저장 처리 콜백
     */
    private final Runnable saveCallback;

    /**
     * 화면 갱신 처리 콜백
     */
    private final Runnable refreshCallback;

    /**
     * 생성자
     * 
     * @param parentFrame      부모 프레임
     * @param workplaceManager 근무지 로직 관리자
     * @param dataProvider     현재 사용자 데이터 제공자
     * @param saveCallback     저장 콜백
     * @param refreshCallback  화면 갱신 콜백
     */
    public WorkplaceController(
            Frame parentFrame,
            WorkplaceManager workplaceManager,
            WorkplaceDataProvider dataProvider,
            Runnable saveCallback,
            Runnable refreshCallback
    ) {
        this.parentFrame = parentFrame;
        this.workplaceManager = workplaceManager;
        this.dataProvider = dataProvider;
        this.saveCallback = saveCallback;
        this.refreshCallback = refreshCallback;
    }

    /**
     * 근무지 추가 다이얼로그를 연다.
     */
    public void openAddWorkplaceDialog() {
        WorkplaceUserContext context = getSafeContext();
        if (context == null) {
            JOptionPane.showMessageDialog(parentFrame, "로그인된 사용자가 없습니다.");
            return;
        }

        AddWorkplaceDialog dialog = new AddWorkplaceDialog(parentFrame,
                formData -> handleAddWorkplace(formData));

        dialog.showDialog();
    }

    /**
     * 근무지 추가 처리
     */
    private void handleAddWorkplace(AddWorkplaceDialog.AddWorkplaceFormData formData) {
        try {
            WorkplaceUserContext context = requireContext();

            String companyName = formData.getCompanyName().trim();
            String hourlyRateText = formData.getHourlyRateText().trim();
            String dayHoursText = formData.getDayHoursText().trim();
            String nightHoursText = formData.getNightHoursText().trim();

            validateWorkplaceInputs(companyName, hourlyRateText, dayHoursText, nightHoursText);

            long hourlyRate = Long.parseLong(hourlyRateText);
            int standardDayHours = Integer.parseInt(dayHoursText);
            int standardNightHours = Integer.parseInt(nightHoursText);

            ensureWorkplaceListExists(context);

            workplaceManager.addWorkplace(
                    context.getWorkplaces(),
                    companyName,
                    hourlyRate,
                    standardDayHours,
                    standardNightHours
            );

            runSaveAndRefresh();
            JOptionPane.showMessageDialog(parentFrame, "새 근무지가 추가되었습니다.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "근무지 추가 중 오류가 발생했습니다.");
        }
    }

    /**
     * 근무지 수정용 선택 다이얼로그를 연다.
     */
    public void openEditWorkplaceDialog() {
        WorkplaceUserContext context = getSafeContext();

        if (context == null || context.getWorkplaces() == null || context.getWorkplaces().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "수정할 근무지가 없습니다.");
            return;
        }

        SelectWorkplaceForEditDialog dialog = new SelectWorkplaceForEditDialog(
                parentFrame,
                buildWorkplaceDisplayList(),
                selectedWorkplaceText -> handleSelectWorkplaceForEdit(selectedWorkplaceText)
        );

        dialog.showDialog();
    }

    /**
     * 수정 대상 근무지 선택 처리
     */
    private void handleSelectWorkplaceForEdit(String selectedWorkplaceText) {
        try {
            if (selectedWorkplaceText == null || selectedWorkplaceText.trim().isEmpty()) {
                throw new IllegalArgumentException("수정할 근무지를 선택해주세요.");
            }

            WorkplaceUserContext context = requireContext();
            Workplace targetWorkplace = workplaceManager.findWorkplaceByDisplayText(
                    context.getWorkplaces(),
                    selectedWorkplaceText
            );

            if (targetWorkplace == null) {
                throw new IllegalArgumentException("선택한 근무지를 찾을 수 없습니다.");
            }

            if (workplaceManager.isWorkplaceInUse(context.getShifts(), targetWorkplace)) {
                throw new IllegalStateException("이 근무지는 현재 일정에서 사용 중이므로 수정할 수 없습니다.");
            }

            openWorkplaceEditFormDialog(targetWorkplace);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "근무지 선택 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 실제 수정 입력 다이얼로그를 연다.
     */
    private void openWorkplaceEditFormDialog(Workplace oldWorkplace) {
        if (oldWorkplace == null) {
            JOptionPane.showMessageDialog(parentFrame, "수정 대상 근무지가 없습니다.");
            return;
        }

        EditWorkplaceDialog dialog = new EditWorkplaceDialog(
                parentFrame,
                oldWorkplace,
                formData -> handleEditWorkplace(oldWorkplace, formData)
        );

        dialog.showDialog();
    }

    /**
     * 근무지 수정 처리
     */
    private void handleEditWorkplace(Workplace oldWorkplace, EditWorkplaceDialog.EditWorkplaceFormData formData) {
        try {
            WorkplaceUserContext context = requireContext();

            String companyName = formData.getCompanyName().trim();
            String hourlyRateText = formData.getHourlyRateText().trim();
            String dayHoursText = formData.getDayHoursText().trim();
            String nightHoursText = formData.getNightHoursText().trim();

            validateWorkplaceInputs(companyName, hourlyRateText, dayHoursText, nightHoursText);

            long hourlyRate = Long.parseLong(hourlyRateText);
            int standardDayHours = Integer.parseInt(dayHoursText);
            int standardNightHours = Integer.parseInt(nightHoursText);

            if (oldWorkplace == null) {
                throw new IllegalStateException("수정 대상 근무지가 없습니다.");
            }

            workplaceManager.updateWorkplace(
                    context.getWorkplaces(),
                    context.getShifts(),
                    oldWorkplace,
                    companyName,
                    hourlyRate,
                    standardDayHours,
                    standardNightHours
            );

            runSaveAndRefresh();
            JOptionPane.showMessageDialog(parentFrame, "근무지 정보가 수정되었습니다.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "근무지 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 근무지 삭제 다이얼로그를 연다.
     */
    public void openDeleteWorkplaceDialog() {
        WorkplaceUserContext context = getSafeContext();

        if (context == null || context.getWorkplaces() == null || context.getWorkplaces().isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "삭제할 근무지가 없습니다.");
            return;
        }

        DeleteWorkplaceDialog dialog = new DeleteWorkplaceDialog(
                parentFrame,
                buildWorkplaceDisplayList(),
                selectedWorkplaceText -> handleDeleteWorkplace(selectedWorkplaceText)
        );

        dialog.showDialog();
    }

    /**
     * 근무지 삭제 처리
     */
    private void handleDeleteWorkplace(String selectedWorkplaceText) {
        try {
            if (selectedWorkplaceText == null || selectedWorkplaceText.trim().isEmpty()) {
                throw new IllegalArgumentException("삭제할 근무지를 선택해주세요.");
            }

            WorkplaceUserContext context = requireContext();

            Workplace targetWorkplace = workplaceManager.findWorkplaceByDisplayText(
                    context.getWorkplaces(),
                    selectedWorkplaceText
            );

            if (targetWorkplace == null) {
                throw new IllegalArgumentException("선택한 근무지를 찾을 수 없습니다.");
            }

            if (workplaceManager.isWorkplaceInUse(context.getShifts(), targetWorkplace)) {
                throw new IllegalStateException("이 근무지는 현재 일정에서 사용 중이므로 삭제할 수 없습니다.");
            }

            int confirm = JOptionPane.showConfirmDialog(
                    parentFrame,
                    "정말로 이 근무지를 삭제하시겠습니까?",
                    "삭제 확인",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            workplaceManager.deleteWorkplace(
                    context.getWorkplaces(),
                    context.getShifts(),
                    targetWorkplace
            );

            runSaveAndRefresh();
            JOptionPane.showMessageDialog(parentFrame, "근무지가 삭제되었습니다.");

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "근무지 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 현재 근무지 목록을 화면 표시용 문자열 리스트로 변환한다.
     */
    public List<String> buildWorkplaceDisplayList() {
        List<String> displayList = new ArrayList<>();

        WorkplaceUserContext context = getSafeContext();
        if (context == null || context.getWorkplaces() == null) {
            return displayList;
        }

        for (Workplace workplace : context.getWorkplaces()) {
            displayList.add(workplaceManager.buildWorkplaceDisplayText(workplace));
        }

        return displayList;
    }

    /**
     * 입력값 검증
     */
    private void validateWorkplaceInputs(
            String companyName,
            String hourlyRateText,
            String dayHoursText,
            String nightHoursText
    ) {
        if (companyName.isEmpty()) {
            throw new IllegalArgumentException("회사명을 입력해주세요.");
        }

        if (hourlyRateText.isEmpty()) {
            throw new IllegalArgumentException("시급을 입력해주세요.");
        }

        if (dayHoursText.isEmpty()) {
            throw new IllegalArgumentException("기본 주간 근무시간을 입력해주세요.");
        }

        if (nightHoursText.isEmpty()) {
            throw new IllegalArgumentException("기본 야간 근무시간을 입력해주세요.");
        }

        long hourlyRate;
        int standardDayHours;
        int standardNightHours;

        try {
            hourlyRate = Long.parseLong(hourlyRateText);
            standardDayHours = Integer.parseInt(dayHoursText);
            standardNightHours = Integer.parseInt(nightHoursText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("시급과 근무시간은 숫자로 입력해주세요.");
        }

        if (hourlyRate <= 0) {
            throw new IllegalArgumentException("시급은 0보다 커야 합니다.");
        }

        if (standardDayHours <= 0 || standardNightHours <= 0) {
            throw new IllegalArgumentException("기본 근무시간은 0보다 커야 합니다.");
        }
    }

    /**
     * 사용자 컨텍스트가 존재하는지 확인하고 반환한다.
     */
    private WorkplaceUserContext requireContext() {
        WorkplaceUserContext context = dataProvider.getUserContext();

        if (context == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        return context;
    }

    /**
     * 예외 없이 현재 컨텍스트를 가져온다.
     */
    private WorkplaceUserContext getSafeContext() {
        return dataProvider.getUserContext();
    }

    /**
     * 근무지 목록이 null이면 초기화한다.
     */
    private void ensureWorkplaceListExists(WorkplaceUserContext context) {
        if (context.getWorkplaces() == null) {
            throw new IllegalStateException("근무지 목록이 초기화되지 않았습니다.");
        }
    }

    /**
     * 저장 후 화면 갱신 공통 처리
     */
    private void runSaveAndRefresh() {
        if (saveCallback != null) {
            saveCallback.run();
        }

        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }
}