package controller;

import java.awt.Frame;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.JOptionPane;

import model.Shift;
import model.Workplace;
import service.ShiftManager;
import service.WorkplaceManager;
import ui.ShiftEditDialog;

/**
 * Shift 기능 전용 컨트롤러
 * 
 * 역할:
 * - 일정 등록 / 수정 / 삭제 흐름을 담당한다.
 * - ShiftEditDialog를 열고 저장/삭제 요청을 연결한다.
 * - 시간 검증, Shift 생성, 목록 반영, 저장 후 새로고침을 담당한다.
 * 
 * 주의:
 * - model.Shift를 새로 만드는 것이 아니라,
 *   기존 Shift 모델을 사용하는 흐름 제어 클래스이다.
 */
/**
 * 일정 저장/수정/삭제 흐름을 담당하는 컨트롤러입니다.
 *
 * 초보 팀원이 자주 수정하는 지점:
 * - 자동 패턴 문자열 해석: convertPatternToUiShiftTypes(...)
 * - 패턴별 기본 시간 규칙: buildShiftForPatternDay(...)
 * - 저장 전 검증 규칙: validateShiftTimeInputs(...)
 *
 * 주의:
 * - 화면 모양은 여기서 바꾸지 않습니다. UI 모양은 ShiftEditDialog 쪽에서 수정합니다.
 * - 이 클래스는 "입력받은 값을 실제 Shift로 저장하는 흐름"을 담당합니다.
 */
public class ShiftController {

	/**
	 * 현재 로그인 사용자 데이터 접근용 인터페이스
	 */
	public interface ShiftDataProvider {
		ShiftUserContext getUserContext();
	}

	/**
	 * Shift 작업에 필요한 사용자 데이터 묶음
	 */
	public static class ShiftUserContext {
		private final String userId;
		private final List<Workplace> workplaces;
		private final List<Shift> shifts;

		public ShiftUserContext(String userId, List<Workplace> workplaces, List<Shift> shifts) {
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

	/** 부모 프레임 */
	private final Frame parentFrame;

	/** Shift 순수 로직 관리자 */
	private final ShiftManager shiftManager;

	/** Workplace 조회용 관리자 */
	private final WorkplaceManager workplaceManager;

	/** 현재 사용자 데이터 제공자 */
	private final ShiftDataProvider dataProvider;

	/** 저장 콜백 */
	private final Runnable saveCallback;

	/** 화면 갱신 콜백 */
	private final Runnable refreshCallback;

	/**
	 * 생성자
	 */
	public ShiftController(
			Frame parentFrame,
			ShiftManager shiftManager,
			WorkplaceManager workplaceManager,
			ShiftDataProvider dataProvider,
			Runnable saveCallback,
			Runnable refreshCallback
	) {
		this.parentFrame = parentFrame;
		this.shiftManager = shiftManager;
		this.workplaceManager = workplaceManager;
		this.dataProvider = dataProvider;
		this.saveCallback = saveCallback;
		this.refreshCallback = refreshCallback;
	}

	/**
	 * Shift 등록/수정 다이얼로그를 연다.
	 * 
	 * @param existingShift 수정 대상 Shift, 신규 등록이면 null
	 * @param dateStringParam 대상 날짜
	 */
	public void openShiftEditDialog(Shift existingShift, String dateStringParam) {
		ShiftUserContext context = getSafeContext();

		if (context == null) {
			JOptionPane.showMessageDialog(parentFrame, "로그인된 사용자가 없습니다.");
			return;
		}

		if (context.getWorkplaces() == null || context.getWorkplaces().isEmpty()) {
			JOptionPane.showMessageDialog(parentFrame, "먼저 근무지를 등록해주세요.");
			return;
		}

		ShiftEditDialog dialog = new ShiftEditDialog(
				parentFrame,
				context.getWorkplaces(),
				existingShift,
				dateStringParam,

				// 저장 버튼 클릭 시
				formData -> handleShiftSave(existingShift, dateStringParam, formData),

				// 삭제 버튼 클릭 시
				() -> handleShiftDelete(existingShift)
		);

		dialog.showDialog();
	}

	/**
	 * Shift 저장 처리
	 */
	private void handleShiftSave(
			Shift existingShift,
			String dateStringParam,
			ShiftEditDialog.ShiftFormData formData
	) {
		try {
			validateShiftTimeInputs(
					formData.getShiftType(),
					formData.getStartTime(),
					formData.getEndTime()
			);

			saveShift(
					existingShift,
					dateStringParam,
					formData.getSelectedWorkplaceText(),
					formData.getShiftType(),
					formData.getStartTime(),
					formData.getEndTime()
			);

			runSaveAndRefresh();

		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(parentFrame, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(parentFrame, "일정 저장 중 오류가 발생했습니다. 날짜/시간 형식을 확인해주세요.");
		}
	}

	/**
	 * 실제 Shift 저장 흐름
	 */
	private void saveShift(
			Shift existingShift,
			String dateStringParam,
			String selectedWorkplaceText,
			String chosenType,
			String inputStart,
			String inputEnd
	) {
		ShiftUserContext context = requireContext();

		Workplace workplace = resolveSelectedWorkplace(selectedWorkplaceText);
		removeExistingShiftIfNeeded(existingShift);

		Shift newShift = buildShiftForSave(
				existingShift,
				workplace,
				dateStringParam,
				chosenType,
				inputStart,
				inputEnd
		);

		appendShift(newShift, context);
	}

	/**
	 * 선택한 문자열에 해당하는 Workplace를 찾는다.
	 */
	private Workplace resolveSelectedWorkplace(String selectedWorkplaceText) {
		ShiftUserContext context = requireContext();

		Workplace workplace = workplaceManager.findWorkplaceByDisplayText(
				context.getWorkplaces(),
				selectedWorkplaceText
		);

		if (workplace == null) {
			throw new IllegalArgumentException("선택한 근무지를 찾을 수 없습니다.");
		}

		return workplace;
	}

	/**
	 * 수정 모드일 때 기존 Shift 제거
	 */
	private void removeExistingShiftIfNeeded(Shift existingShift) {
		ShiftUserContext context = getSafeContext();

		if (existingShift == null || context == null || context.getShifts() == null) {
			return;
		}

		context.getShifts().removeIf(shift -> shift.getId().equals(existingShift.getId()));
	}

	/**
	 * 저장용 Shift 객체 생성
	 */
	private Shift buildShiftForSave(
			Shift existingShift,
			Workplace workplace,
			String dateStringParam,
			String chosenType,
			String inputStart,
			String inputEnd
	) {
		String shiftId = (existingShift != null)
				? existingShift.getId()
				: String.valueOf(System.currentTimeMillis());

		return shiftManager.createShiftFromUiInput(
				shiftId,
				workplace,
				dateStringParam,
				inputStart,
				inputEnd,
				chosenType
		);
	}

	/**
	 * Shift 목록에 추가
	 */
	private void appendShift(Shift newShift, ShiftUserContext context) {
		if (context == null) {
			throw new IllegalStateException("로그인된 사용자가 없습니다.");
		}

		if (context.getShifts() == null) {
			throw new IllegalStateException("Shift 목록이 초기화되지 않았습니다.");
		}

		context.getShifts().add(newShift);
	}

	/**
	 * Shift 삭제 처리
	 */
	private void handleShiftDelete(Shift existingShift) {
		try {
			deleteShift(existingShift);
			runSaveAndRefresh();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(parentFrame, "일정 삭제 중 오류가 발생했습니다.");
		}
	}

	/**
	 * 기존 Shift 삭제
	 */
	private void deleteShift(Shift existingShift) {
		if (!canDeleteShift(existingShift)) {
			return;
		}

		removeShiftById(existingShift);
	}

	/**
	 * 삭제 가능 여부 검사
	 */
	private boolean canDeleteShift(Shift existingShift) {
		ShiftUserContext context = getSafeContext();

		if (existingShift == null) {
			return false;
		}

		if (context == null) {
			return false;
		}

		if (context.getShifts() == null) {
			return false;
		}

		return true;
	}

	/**
	 * Shift ID 기준 삭제
	 */
	private void removeShiftById(Shift existingShift) {
		ShiftUserContext context = requireContext();
		context.getShifts().removeIf(shift -> shift.getId().equals(existingShift.getId()));
	}

	/**
	 * 패턴 문자열을 UI용 근무유형 배열로 변환합니다.
	 *
	 * 초보 팀원 패턴 추가 규칙:
	 * - openAutoPatternDialog()에서 "주-야-휴"처럼 문자열을 추가하면
	 *   여기서 각 토큰을 어떤 근무유형으로 볼지 정해줘야 합니다.
	 * - 지금은 아래 규칙을 사용합니다.
	 *   "주" -> 주간근무
	 *   "야" -> 야간근무
	 *   "비", "휴", "휴무" -> 휴무
	 *
	 * 만약 "오전", "오후", "당직" 같은 새 토큰을 만들면
	 * 반드시 이 메서드에 해석 규칙을 추가해야 합니다.
	 */
	public String[] convertPatternToUiShiftTypes(String[] rawPatternArray) {
		String[] safePatternArray = new String[rawPatternArray.length];

		for (int i = 0; i < rawPatternArray.length; i++) {
			String currentWord = rawPatternArray[i];

			if (currentWord.equals("주")) {
				safePatternArray[i] = "주간근무";
			} else if (currentWord.equals("야")) {
				safePatternArray[i] = "야간근무";
			} else if (currentWord.equals("비") || currentWord.equals("휴") || currentWord.equals("휴무")) {
				safePatternArray[i] = "휴무";
			} else {
				safePatternArray[i] = currentWord;
			}
		}

		return safePatternArray;
	}

	/**
	 * 자동 패턴 생성용 하루치 Shift를 만듭니다.
	 *
	 * 현재 기본 시간 규칙:
	 * - 주간근무: 09:00 ~ 18:00
	 * - 야간근무: 22:00 ~ 06:00
	 * - 휴무: 09:00 ~ 18:00 형식으로 생성되지만,
	 *   실제 급여 계산은 ShiftType.OFF 기준으로 처리됩니다.
	 *
	 * 패턴별 시간 규칙을 바꾸고 싶으면 이 메서드를 수정하면 됩니다.
	 */
	public Shift buildShiftForPatternDay(
			Workplace workplace,
			LocalDate currentDate,
			String generatedShiftType,
			long uniqueSeed
	) {
		String startTime = "09:00";
		String endTime = "18:00";

		if (generatedShiftType.equals("야간근무")) {
			startTime = "22:00";
			endTime = "06:00";
		}

		return shiftManager.createShiftFromUiInput(
				String.valueOf(uniqueSeed),
				workplace,
				currentDate.toString(),
				startTime,
				endTime,
				generatedShiftType
		);
	}

	/**
	 * 패턴으로 생성된 Shift들을 한 번에 추가
	 */
	public void appendPatternShiftsAndPersist(List<Shift> newShifts) {
		ShiftUserContext context = requireContext();

		if (context.getShifts() == null) {
			throw new IllegalStateException("Shift 목록이 초기화되지 않았습니다.");
		}

		context.getShifts().addAll(newShifts);
		runSaveAndRefresh();
	}

	/**
	 * 시간 문자열이 HH:mm 형식인지 검사
	 */
	private boolean isValidShiftTimeFormat(String timeText) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime.parse(timeText, formatter);
			return true;
		} catch (DateTimeParseException ex) {
			return false;
		}
	}

	/**
	 * 시간 입력 검증
	 */
	private void validateShiftTimeInputs(String chosenType, String inputStart, String inputEnd) {
		if (!isValidShiftTimeFormat(inputStart)) {
			throw new IllegalArgumentException("시작 시간은 HH:mm 형식으로 입력해주세요. 예: 09:00");
		}

		if (!isValidShiftTimeFormat(inputEnd)) {
			throw new IllegalArgumentException("종료 시간은 HH:mm 형식으로 입력해주세요. 예: 18:00");
		}

		if (!chosenType.equals("휴무") && inputStart.equals(inputEnd)) {
			throw new IllegalArgumentException("시작 시간과 종료 시간은 같을 수 없습니다.");
		}
	}

	/**
	 * 현재 사용자 컨텍스트 반환
	 */
	private ShiftUserContext requireContext() {
		ShiftUserContext context = dataProvider.getUserContext();

		if (context == null) {
			throw new IllegalStateException("로그인된 사용자가 없습니다.");
		}

		return context;
	}

	/**
	 * 예외 없이 현재 사용자 컨텍스트 반환
	 */
	private ShiftUserContext getSafeContext() {
		return dataProvider.getUserContext();
	}

	/**
	 * 저장 및 화면 갱신 공통 처리
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
