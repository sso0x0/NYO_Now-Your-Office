package ui;

import model.Shift;
import model.ShiftType;
import model.TotalSummary;
import service.StatisticsService;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * 왼쪽 사이드바에서 재사용하는 급여 카드/그래프 패널입니다.
 *
 * 초보 팀원 수정 포인트:
 * - 급여 카드 문구/숫자 스타일: 생성자, refreshSalaryCard()
 * - 비율 그래프 모양: RatioGraphPanel
 * - 카드 크기/여백: 생성자 내부 preferred/max size
 */
public class DashboardPanel extends JPanel {

	private final StatisticsService statisticsService;

	private final JPanel salaryCardPanel;
	private final JPanel graphSectionPanel;

	private final JLabel netSalaryLabel;
	private final JLabel totalSalaryLabel;

	private final RatioGraphPanel graphPanel;

	private List<Shift> currentShifts = Collections.emptyList();
	private double currentTaxRate = 0.0;

	public DashboardPanel(StatisticsService statisticsService) {
		this.statisticsService = statisticsService;

		// 이 패널 자체는 화면에 직접 add해서 쓰지 않을 수도 있으므로
		// 내부 조립용 컨테이너 역할만 하게 둡니다.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.WHITE);

		// =========================
		// 급여 카드 패널
		// =========================
		salaryCardPanel = new JPanel(new BorderLayout(0, 10));
		salaryCardPanel.setBackground(Color.WHITE);
		salaryCardPanel.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
						BorderFactory.createEmptyBorder(18, 18, 18, 18)));
		salaryCardPanel.setMaximumSize(new Dimension(270, 132));
		salaryCardPanel.setPreferredSize(new Dimension(270, 132));
		salaryCardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel cardTitle = new JLabel("이번 달 예상 실수령액");
		cardTitle.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		cardTitle.setForeground(new Color(139, 149, 161));

		netSalaryLabel = new JLabel("0 원");
		netSalaryLabel.setFont(new Font("맑은 고딕", Font.BOLD, 31));
		netSalaryLabel.setForeground(new Color(37, 99, 235));

		totalSalaryLabel = new JLabel("세전 기준: 0 원");
		totalSalaryLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		totalSalaryLabel.setForeground(new Color(139, 149, 161));

		salaryCardPanel.add(cardTitle, BorderLayout.NORTH);
		salaryCardPanel.add(netSalaryLabel, BorderLayout.CENTER);
		salaryCardPanel.add(totalSalaryLabel, BorderLayout.SOUTH);

		// =========================
		// 그래프 섹션 패널
		// =========================
		graphSectionPanel = new JPanel();
		graphSectionPanel.setLayout(new BoxLayout(graphSectionPanel, BoxLayout.Y_AXIS));
		graphSectionPanel.setBackground(Color.WHITE);
		graphSectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		graphSectionPanel.setMaximumSize(new Dimension(270, 220));
		graphSectionPanel.setPreferredSize(new Dimension(270, 220));
		graphSectionPanel.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
						BorderFactory.createEmptyBorder(16, 16, 16, 16)));

		JLabel graphTitle = new JLabel("근무 비율");
		graphTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		graphTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		graphTitle.setForeground(new Color(31, 41, 55));

		JLabel graphSubtitle = new JLabel("이번 달 주간/야간 근무 분포");
		graphSubtitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		graphSubtitle.setForeground(new Color(107, 114, 128));
		graphSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

		graphPanel = new RatioGraphPanel();
		graphPanel.setPreferredSize(new Dimension(240, 130));
		graphPanel.setMaximumSize(new Dimension(240, 130));
		graphPanel.setBackground(new Color(249, 250, 251));
		graphPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
				BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		graphPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		graphSectionPanel.add(graphTitle);
		graphSectionPanel.add(Box.createVerticalStrut(4));
		graphSectionPanel.add(graphSubtitle);
		graphSectionPanel.add(Box.createVerticalStrut(12));
		graphSectionPanel.add(graphPanel);
	}

	public void refresh(List<Shift> shifts, double taxRate) {
		if (shifts == null) {
			this.currentShifts = Collections.emptyList();
		} else {
			this.currentShifts = shifts;
		}

		this.currentTaxRate = taxRate;

		refreshSalaryCard();
		refreshRatioGraph();
	}

	private void refreshSalaryCard() {
		TotalSummary summary = statisticsService.calculateTotalSummary(currentShifts);

		long totalGrossSalary = summary.getTotalExpectedSalary();
		long calculatedNetSalary = Math.round(totalGrossSalary * (1.0 - currentTaxRate));

		netSalaryLabel.setText(String.format("%,d 원", calculatedNetSalary));
		totalSalaryLabel.setText(String.format("세전 기준: %,d 원", totalGrossSalary));
	}

	private void refreshRatioGraph() {
		int dayCount = 0;
		int nightCount = 0;

		for (Shift shift : currentShifts) {
			if (shift.getShiftType() == ShiftType.DAY) {
				dayCount++;
			} else if (shift.getShiftType() == ShiftType.NIGHT) {
				nightCount++;
			}
		}

		int total = dayCount + nightCount;
		int dayRatio = 0;
		int nightRatio = 0;

		if (total > 0) {
			dayRatio = (int) (((double) dayCount / total) * 100);
			nightRatio = (int) (((double) nightCount / total) * 100);
		}

		graphPanel.setGraphData(dayRatio, nightRatio, dayCount, nightCount);
	}

	/**
	 * 주간/야간 비율 그래프 전용 패널
	 * 
	 * 역할: - 주간/야간 비율을 막대 그래프로 그림 - 퍼센트와 실제 건수도 함께 표시
	 * 
	 * 주의: - 색상은 기존 UI 톤과 맞춤 - 화면 크기가 조금 바뀌어도 자연스럽게 보이도록 상대 좌표를 사용
	 */
	private static class RatioGraphPanel extends JPanel {
		private int dayRatio = 0;
		private int nightRatio = 0;
		private int dayCount = 0;
		private int nightCount = 0;

		/**
		 * 그래프 표시 데이터를 설정합니다.
		 * 
		 * @param dayRatio   주간 비율(%)
		 * @param nightRatio 야간 비율(%)
		 * @param dayCount   주간 건수
		 * @param nightCount 야간 건수
		 */
		public void setGraphData(int dayRatio, int nightRatio, int dayCount, int nightCount) {
			this.dayRatio = dayRatio;
			this.nightRatio = nightRatio;
			this.dayCount = dayCount;
			this.nightCount = nightCount;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			int width = getWidth();
			int height = getHeight();

			// 여백 설정
			int topMargin = 20;
			int bottomMargin = 35;
			int leftMargin = 25;
			int chartHeight = height - topMargin - bottomMargin;

			// 막대 너비와 위치 계산
			int barWidth = 55;
			int gap = 45;

			int totalChartWidth = barWidth * 2 + gap;
			int startX = (width - totalChartWidth) / 2;

			int dayX = startX;
			int nightX = startX + barWidth + gap;

			int baseY = topMargin + chartHeight;

			// 비율을 실제 높이로 변환
			int dayBarHeight = (int) Math.round(chartHeight * (dayRatio / 100.0));
			int nightBarHeight = (int) Math.round(chartHeight * (nightRatio / 100.0));

			Graphics2D g2 = (Graphics2D) g.create();

			// 축 선
			g2.setColor(new Color(220, 220, 220));
			g2.drawLine(leftMargin, baseY, width - leftMargin, baseY);

			// 주간 막대
			g2.setColor(new Color(49, 130, 246));
			g2.fillRoundRect(dayX, baseY - dayBarHeight, barWidth, dayBarHeight, 12, 12);

			// 야간 막대
			g2.setColor(new Color(230, 73, 128));
			g2.fillRoundRect(nightX, baseY - nightBarHeight, barWidth, nightBarHeight, 12, 12);

			// 비율 텍스트
			g2.setColor(new Color(60, 60, 60));
			g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
			g2.drawString(dayRatio + "%", dayX + 12, baseY - dayBarHeight - 8);
			g2.drawString(nightRatio + "%", nightX + 12, baseY - nightBarHeight - 8);

			// 하단 라벨
			g2.setFont(new Font("맑은 고딕", Font.BOLD, 12));
			g2.drawString("주간", dayX + 14, baseY + 18);
			g2.drawString("야간", nightX + 14, baseY + 18);

			// 실제 건수 표시
			g2.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
			g2.setColor(new Color(120, 120, 120));
			g2.drawString(dayCount + "건", dayX + 12, baseY + 32);
			g2.drawString(nightCount + "건", nightX + 12, baseY + 32);

			g2.dispose();
		}
	}

	public JPanel getSalaryCardPanel() {
		return salaryCardPanel;
	}

	public JPanel getGraphSectionPanel() {
		return graphSectionPanel;
	}

	

}
