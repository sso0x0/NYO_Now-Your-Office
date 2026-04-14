package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

/**
 * 마이페이지 전용 패널
 * 
 * 역할:
 * - 사용자 정보 수정 UI를 구성합니다.
 * - 회사명 / 계약 형태 / 시급 / 세금 공제 유형 입력창을 제공합니다.
 * - 저장 / 메인 복귀 버튼을 제공합니다.
 * 
 * 주의:
 * - 실제 저장 처리와 화면 전환은 여기서 하지 않습니다.
 * - 필요한 동작은 외부(WorkerCalendarAppFinal) 콜백으로 위임합니다.
 */
/**
 * 마이페이지 입력 화면입니다.
 *
 * 이 클래스는 입력 UI만 담당합니다.
 * 실제 저장은 WorkerCalendarAppFinal.handleMyPageSave(...)에서 처리합니다.
 *
 * 초보 팀원 수정 포인트:
 * - 입력칸 추가/삭제
 * - 버튼 문구/배치
 * - 세금 콤보박스 항목
 */
public class MyPagePanel extends JPanel {

	/**
	 * 저장 버튼 클릭 시 외부로 전달할 폼 데이터
	 */
	public static class MyPageFormData {
		private final String companyName;
		private final String contractType;
		private final String hourlyWageText;
		private final int taxDropdownIndex;

		/**
		 * 마이페이지 입력 데이터 생성자
		 * 
		 * @param companyName      회사명
		 * @param contractType     계약 형태
		 * @param hourlyWageText   시급 문자열
		 * @param taxDropdownIndex 세금 콤보박스 선택 인덱스
		 */
		public MyPageFormData(String companyName, String contractType, String hourlyWageText, int taxDropdownIndex) {
			this.companyName = companyName;
			this.contractType = contractType;
			this.hourlyWageText = hourlyWageText;
			this.taxDropdownIndex = taxDropdownIndex;
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
		 * 계약 형태 반환
		 * 
		 * @return 계약 형태
		 */
		public String getContractType() {
			return contractType;
		}

		/**
		 * 시급 문자열 반환
		 * 
		 * @return 시급 문자열
		 */
		public String getHourlyWageText() {
			return hourlyWageText;
		}

		/**
		 * 세금 콤보박스 선택 인덱스 반환
		 * 
		 * @return 세금 선택 인덱스
		 */
		public int getTaxDropdownIndex() {
			return taxDropdownIndex;
		}
	}

	/**
	 * 저장 요청 콜백
	 */
	public interface OnSaveListener {
		void onSave(MyPageFormData formData);
	}

	/**
	 * 메인 복귀 요청 콜백
	 */
	public interface OnBackListener {
		void onBack();
	}

	// 입력 필드
	private JTextField companyInput;
	private JTextField contractInput;
	private JTextField wageInput;
	private JComboBox<String> taxDropdown;

	// 외부 위임 콜백
	private final OnSaveListener onSaveListener;
	private final OnBackListener onBackListener;

	// 색상 테마
	private final Color colorBackgroundGray;
	private final Color colorCardWhite;
	private final Color colorTextDark;
	private final Color colorTossBlue;

	/**
	 * 생성자
	 * 
	 * @param colorBackgroundGray 배경 회색
	 * @param colorCardWhite      카드 흰색
	 * @param colorTextDark       진한 글자색
	 * @param colorTossBlue       강조 파란색
	 * @param onSaveListener      저장 콜백
	 * @param onBackListener      뒤로가기 콜백
	 */
	public MyPagePanel(
			Color colorBackgroundGray,
			Color colorCardWhite,
			Color colorTextDark,
			Color colorTossBlue,
			OnSaveListener onSaveListener,
			OnBackListener onBackListener) {

		this.colorBackgroundGray = colorBackgroundGray;
		this.colorCardWhite = colorCardWhite;
		this.colorTextDark = colorTextDark;
		this.colorTossBlue = colorTossBlue;
		this.onSaveListener = onSaveListener;
		this.onBackListener = onBackListener;

		initializeUI();
	}

	/**
	 * 마이페이지 전체 UI를 구성합니다.
	 */
	private void initializeUI() {
		setLayout(new BorderLayout());
		setBackground(colorBackgroundGray);

		JPanel centerContainer = new JPanel(new GridBagLayout());
		centerContainer.setBackground(colorBackgroundGray);

		JPanel formBox = new JPanel(new GridLayout(5, 2, 15, 20));
		formBox.setBackground(colorCardWhite);
		formBox.setBorder(new EmptyBorder(40, 40, 40, 40));
		formBox.setPreferredSize(new Dimension(500, 300));

		companyInput = new JTextField();
		contractInput = new JTextField();
		wageInput = new JTextField();

		taxDropdown = new JComboBox<>(new String[] {
				"미공제 (0%)",
				"프리랜서 (3.3%)",
				"4대보험 (9.32%)"
		});

		formBox.add(new JLabel("소속 회사명:"));
		formBox.add(companyInput);
		formBox.add(new JLabel("계약 형태(정규직/알바 등):"));
		formBox.add(contractInput);
		formBox.add(new JLabel("나의 기본 시급(원):"));
		formBox.add(wageInput);
		formBox.add(new JLabel("세금 공제 유형:"));
		formBox.add(taxDropdown);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
		btnPanel.setBackground(colorCardWhite);

		JButton goBackBtn = new JButton("메인으로");
		goBackBtn.setPreferredSize(new Dimension(130, 40));
		goBackBtn.setBackground(Color.GRAY);
		goBackBtn.setForeground(Color.WHITE);
		goBackBtn.setFocusPainted(false);

		JButton saveInfoBtn = new JButton("정보 업데이트");
		saveInfoBtn.setPreferredSize(new Dimension(130, 40));
		saveInfoBtn.setBackground(colorTossBlue);
		saveInfoBtn.setForeground(Color.WHITE);
		saveInfoBtn.setFocusPainted(false);

		// 메인 복귀 요청
		goBackBtn.addActionListener(e -> {
			if (onBackListener != null) {
				onBackListener.onBack();
			}
		});

		// 저장 요청
		saveInfoBtn.addActionListener(e -> {
			if (onSaveListener != null) {
				MyPageFormData formData = new MyPageFormData(
						companyInput.getText(),
						contractInput.getText(),
						wageInput.getText(),
						taxDropdown.getSelectedIndex()
				);
				onSaveListener.onSave(formData);
			}
		});

		btnPanel.add(goBackBtn);
		btnPanel.add(saveInfoBtn);

		JPanel contentBox = new JPanel(new BorderLayout());
		contentBox.setBackground(colorCardWhite);
		contentBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
		contentBox.add(formBox, BorderLayout.CENTER);
		contentBox.add(btnPanel, BorderLayout.SOUTH);

		JLabel titleLbl = new JLabel("⚙️ 마이페이지 및 급여 설정", SwingConstants.CENTER);
		titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 22));
		titleLbl.setBorder(new EmptyBorder(30, 0, 10, 0));
		titleLbl.setForeground(colorTextDark);

		centerContainer.add(contentBox);

		add(titleLbl, BorderLayout.NORTH);
		add(centerContainer, BorderLayout.CENTER);
	}

	/**
	 * 외부에서 현재 사용자 정보를 화면에 채웁니다.
	 * 
	 * 역할:
	 * - 마이페이지 진입 시 현재 저장된 사용자 정보를 입력창에 반영합니다.
	 * 
	 * @param companyName   회사명
	 * @param contractType  계약 형태
	 * @param hourlyWage    시급
	 * @param taxRate       세율
	 */
	public void setUserInfo(String companyName, String contractType, int hourlyWage, double taxRate) {
		companyInput.setText(companyName);
		contractInput.setText(contractType);
		wageInput.setText(String.valueOf(hourlyWage));

		if (taxRate == 0.0) {
			taxDropdown.setSelectedIndex(0);
		} else if (taxRate == 0.033) {
			taxDropdown.setSelectedIndex(1);
		} else {
			taxDropdown.setSelectedIndex(2);
		}
	}
}
