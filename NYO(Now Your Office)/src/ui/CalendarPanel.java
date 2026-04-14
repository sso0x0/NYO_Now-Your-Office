package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ui.WorkerCalendarAppFinal.ModernButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * CalendarPanel
 *
 * 역할: - 달력 헤더 표시 - 날짜 그리드 표시 - 날짜 선택 처리 - 일정 배지 표시 - 하단 상세 카드 표시
 *
 * 중요: - 저장/수정/삭제의 실제 처리 로직은 외부(WorkerCalendarAppFinal)에 위임 - 현재 단계에서는 UI 분리 목적이
 * 우선
 */
/**
 * 메인 달력 UI를 담당하는 패널입니다.
 *
 * 초보 팀원 수정 포인트:
 * - 달력 셀 모양/크기: createCalendarGrid(), refreshCalendarView()
 * - 일정 배지 모양: createScheduleBadge(...)
 * - 일정이 많을 때 표시 규칙: refreshCalendarView(), createMoreBadge(...)
 * - 하단 상세 카드 구성: createDailyDetailCard(...)
 */
public class CalendarPanel extends JPanel {

   /**
    * 날짜별 일정 렌더링에 사용할 간단한 UI용 데이터 구조
    *
    * 현재 WorkerCalendarAppFinal의 legacy Schedule 구조를 직접 참조하지 않기 위해 내부 DTO로 분리
    */
   public static class CalendarScheduleItem {
      private final String id;
      private final String date;
      private final String shiftType;
      private final String workplaceName;
      private final String startTime;
      private final String endTime;
      private final Color badgeColor;

      public CalendarScheduleItem(String id, String date, String shiftType, String workplaceName, String startTime,
            String endTime, Color badgeColor) {
         this.id = id;
         this.date = date;
         this.shiftType = shiftType;
         this.workplaceName = workplaceName;
         this.startTime = startTime;
         this.endTime = endTime;
         this.badgeColor = badgeColor;
      }

      public String getId() {
         return id;
      }

      public String getDate() {
         return date;
      }

      public String getShiftType() {
         return shiftType;
      }

      public String getWorkplaceName() {
         return workplaceName;
      }

      public String getStartTime() {
         return startTime;
      }

      public String getEndTime() {
         return endTime;
      }

      public Color getBadgeColor() {
         return badgeColor;
      }
   }

   /**
    * 날짜 클릭 / 일정 클릭 / 월 변경을 외부에 알리기 위한 리스너
    */
   public interface CalendarPanelListener {
      void onPreviousMonth();

      void onNextMonth();

      void onDateSelected(String dateString);

      void onScheduleClicked(String scheduleId, String dateString);

      // 상단 우측 버튼 콜백
      void onBoardClicked();
      void onMyPageClicked();
      void onExportClicked();
      void onLogoutClicked();
   }

   private final Color colorBackgroundGray;
   private final Color colorCardWhite;
   private final Color colorTextDark;
   private final Color colorTossBlue;
   private final Color colorAccentRed;
   private final Color colorBorderLine;
   private final Color colorPastelBlue;

   private final CalendarPanelListener listener;

   private final JLabel monthTitleLabel;
   private JPanel datesGridPanel;
   private JPanel bottomDetailPanel;

   private int currentYear;
   private int currentMonth;
   private String currentDateString;
   private String selectedDateString;

   private List<CalendarScheduleItem> scheduleItems = new ArrayList<>();

   /**
    * 생성자
    */
   public CalendarPanel(int currentYear, int currentMonth, String currentDateString, String selectedDateString,
         Color colorBackgroundGray, Color colorCardWhite, Color colorTextDark, Color colorTossBlue,
         Color colorAccentRed, Color colorBorderLine, Color colorPastelBlue, CalendarPanelListener listener) {
      this.currentYear = currentYear;
      this.currentMonth = currentMonth;
      this.currentDateString = currentDateString;
      this.selectedDateString = selectedDateString;

      this.colorBackgroundGray = colorBackgroundGray;
      this.colorCardWhite = colorCardWhite;
      this.colorTextDark = colorTextDark;
      this.colorTossBlue = colorTossBlue;
      this.colorAccentRed = colorAccentRed;
      this.colorBorderLine = colorBorderLine;
      this.colorPastelBlue = colorPastelBlue;

      this.listener = listener;

      setLayout(new BorderLayout(0, 10));
      setBackground(colorBackgroundGray);

      // 헤더
      JPanel headerPanel = createCalendarHeader();

      // 그리드
      JPanel gridPanel = createCalendarGrid();

      // 하단 상세
      bottomDetailPanel = createBottomDetailCards();

      add(headerPanel, BorderLayout.NORTH);
      add(gridPanel, BorderLayout.CENTER);
      add(bottomDetailPanel, BorderLayout.SOUTH);

      monthTitleLabel = findMonthTitleLabel(headerPanel);
   }

   /**
    * 외부에서 달력 데이터 전체 갱신
    */
   public void refreshCalendar(int currentYear, int currentMonth, String currentDateString, String selectedDateString,
         List<CalendarScheduleItem> scheduleItems) {
      this.currentYear = currentYear;
      this.currentMonth = currentMonth;
      this.currentDateString = currentDateString;
      this.selectedDateString = selectedDateString;
      this.scheduleItems = (scheduleItems == null) ? new ArrayList<>() : scheduleItems;

      refreshCalendarView();
   }

   /**
    * 현재 선택 날짜 반환
    */
   public String getSelectedDateString() {
      return selectedDateString;
   }

   /**
    * 달력 헤더 생성
    */
   private JPanel createCalendarHeader() {
      JPanel header = new JPanel(new BorderLayout());
      header.setBackground(colorBackgroundGray);
      header.setBorder(new EmptyBorder(2, 0, 6, 0));

      JPanel centerNav = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 0));
      centerNav.setBackground(colorBackgroundGray);

      JButton prevBtn = new JButton("◀");
      prevBtn.setContentAreaFilled(false);
      prevBtn.setBorderPainted(false);
      prevBtn.setFocusPainted(false);
      prevBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
      prevBtn.setForeground(new Color(75, 85, 99));

      JLabel titleLabel = new JLabel(currentYear + "년 " + currentMonth + "월", SwingConstants.CENTER);
      titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
      titleLabel.setForeground(new Color(17, 24, 39));

      JButton nextBtn = new JButton("▶");
      nextBtn.setContentAreaFilled(false);
      nextBtn.setBorderPainted(false);
      nextBtn.setFocusPainted(false);
      nextBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
      nextBtn.setForeground(new Color(75, 85, 99));

      prevBtn.addActionListener(e -> {
         if (listener != null) {
            listener.onPreviousMonth();
         }
      });

      nextBtn.addActionListener(e -> {
         if (listener != null) {
            listener.onNextMonth();
         }
      });

      centerNav.add(prevBtn);
      centerNav.add(titleLabel);
      centerNav.add(nextBtn);

      header.add(centerNav, BorderLayout.WEST);

      // 상단 우측 버튼 패널
      JPanel quickActionPanel = createQuickActionPanel();
      header.add(quickActionPanel, BorderLayout.EAST);

      return header;
   }

   /**
    * 상단 우측 버튼 패널 생성
    */
   private JPanel createQuickActionPanel() {
      JPanel quickActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
      quickActionPanel.setBackground(colorBackgroundGray);
      quickActionPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

      ModernButton boardButton = new ModernButton("통합 게시판", Color.WHITE, new Color(31, 41, 55));
      ModernButton myPageButton = new ModernButton("마이페이지", Color.WHITE, new Color(31, 41, 55));
      ModernButton exportButton = new ModernButton("명세서 추출", new Color(15, 118, 110), colorCardWhite);
      ModernButton logoutButton = new ModernButton("로그아웃", new Color(239, 68, 68), colorCardWhite);

      boardButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(4, 10, 4, 10)));
      myPageButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(4, 10, 4, 10)));

      // 외부(WorkerCalendarAppFinal)로 신호만 보냅니다.
      boardButton.addActionListener(e -> {
         if (listener != null) listener.onBoardClicked();
      });

      myPageButton.addActionListener(e -> {
         if (listener != null) listener.onMyPageClicked();
      });

      exportButton.addActionListener(e -> {
         if (listener != null) listener.onExportClicked();
      });

      logoutButton.addActionListener(e -> {
         if (listener != null) listener.onLogoutClicked();
      });

      quickActionPanel.add(boardButton);
      quickActionPanel.add(myPageButton);
      quickActionPanel.add(exportButton);
      quickActionPanel.add(logoutButton);

      return quickActionPanel;
   }

   /**
    * 헤더에서 month title label 찾기
    */
   private JLabel findMonthTitleLabel(JPanel headerPanel) {
      JPanel centerNav = (JPanel) headerPanel.getComponent(0);
      for (Component component : centerNav.getComponents()) {
         if (component instanceof JLabel) {
            return (JLabel) component;
         }
      }
      throw new IllegalStateException("monthTitleLabel을 찾을 수 없습니다.");
   }

   /**
    * 달력 그리드 생성
    */
   private JPanel createCalendarGrid() {
      JPanel gridWrapper = new JPanel(new BorderLayout());
      gridWrapper.setBackground(colorCardWhite);
      gridWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));
      gridWrapper.setPreferredSize(new Dimension(0, 560));

      JPanel headerDaysPanel = new JPanel(new GridLayout(1, 7));
      headerDaysPanel.setBackground(new Color(249, 250, 251));
      headerDaysPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

      String[] daysOfWeek = { "일", "월", "화", "수", "목", "금", "토" };
      for (int i = 0; i < daysOfWeek.length; i++) {
         JLabel lbl = new JLabel(daysOfWeek[i], SwingConstants.CENTER);
         lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
         lbl.setBorder(new EmptyBorder(10, 0, 10, 0));

         if (i == 0) {
            lbl.setForeground(colorAccentRed);
         } else if (i == 6) {
            lbl.setForeground(colorTossBlue);
         }
         headerDaysPanel.add(lbl);
      }

      datesGridPanel = new JPanel(new GridLayout(6, 7));
      datesGridPanel.setBackground(colorBorderLine);

      gridWrapper.add(headerDaysPanel, BorderLayout.NORTH);
      gridWrapper.add(datesGridPanel, BorderLayout.CENTER);

      return gridWrapper;
   }

   /**
    * 하단 상세 카드 영역 생성
    *
    * 역할:
    * - 오늘 일정 카드와 선택한 날짜 일정 카드를
    *   한 줄에 좌우로 나란히 배치합니다.
    */
   private JPanel createBottomDetailCards() {
      JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
      panel.setBackground(colorBackgroundGray);
      panel.setPreferredSize(new Dimension(0, 185));
      panel.setBorder(new EmptyBorder(4, 0, 0, 0));
      return panel;
   }

   /**
    * 달력 전체 새로고침
    */
   private void refreshCalendarView() {
      if (currentMonth > 12) {
         currentMonth = 1;
         currentYear++;
      }
      if (currentMonth < 1) {
         currentMonth = 12;
         currentYear--;
      }

      monthTitleLabel.setText(currentYear + "년 " + currentMonth + "월");
      datesGridPanel.removeAll();

      Calendar cal = Calendar.getInstance();
      cal.set(currentYear, currentMonth - 1, 1);
      int maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      int startDayColumn = cal.get(Calendar.DAY_OF_WEEK) - 1;

      for (int i = 0; i < startDayColumn; i++) {
         JPanel emptyCell = new JPanel();
         emptyCell.setBackground(colorCardWhite);
         emptyCell.setBorder(BorderFactory.createLineBorder(colorBorderLine, 1));
         datesGridPanel.add(emptyCell);
      }

      for (int i = 1; i <= maxDaysInMonth; i++) {
         final String dateString = String.format("%04d-%02d-%02d", currentYear, currentMonth, i);
         JPanel dayCell = new JPanel(new BorderLayout());

         if (dateString.equals(selectedDateString)) {
            dayCell.setBackground(new Color(239, 246, 255));
         } else if (dateString.equals(currentDateString)) {
            // 오늘 날짜
            dayCell.setBackground(new Color(200, 225, 255));
         } else {
            // 기본
            dayCell.setBackground(colorCardWhite);
         }
         dayCell.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));

         JLabel dateNumLabel = new JLabel(String.valueOf(i));
         dateNumLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
         dateNumLabel.setBorder(new EmptyBorder(5, 8, 0, 0));
         dateNumLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

         int currentColumn = (startDayColumn + i - 1) % 7;
         if (currentColumn == 0) {
            dateNumLabel.setForeground(colorAccentRed);
         } else if (currentColumn == 6) {
            dateNumLabel.setForeground(colorTossBlue);
         } else {
            dateNumLabel.setForeground(colorTextDark);
         }

         dayCell.add(dateNumLabel, BorderLayout.NORTH);

         dateNumLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               selectedDateString = dateString;
               if (listener != null) {
                  listener.onDateSelected(dateString);
               }
            }
         });

         JPanel scheduleBadgePanel = new JPanel();
         scheduleBadgePanel.setLayout(new BoxLayout(scheduleBadgePanel, BoxLayout.Y_AXIS));
         scheduleBadgePanel.setOpaque(false);
         scheduleBadgePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

         List<CalendarScheduleItem> dailySchedules = getSchedulesForDate(dateString);
         int visibleCount = Math.min(2, dailySchedules.size());

         for (int scheduleIndex = 0; scheduleIndex < visibleCount; scheduleIndex++) {
            CalendarScheduleItem schedule = dailySchedules.get(scheduleIndex);
            JButton scheduleBtn = createScheduleBadge(schedule.getShiftType(), schedule.getBadgeColor(),
                  schedule.getId(), dateString);
            scheduleBadgePanel.add(scheduleBtn);
            scheduleBadgePanel.add(Box.createVerticalStrut(3));
         }

         if (dailySchedules.size() > visibleCount) {
            JButton moreButton = createMoreBadge(dailySchedules.size() - visibleCount, dateString);
            scheduleBadgePanel.add(moreButton);
         }

         dayCell.add(scheduleBadgePanel, BorderLayout.CENTER);

         dayCell.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               selectedDateString = dateString;
               if (listener != null) {
                  listener.onDateSelected(dateString);
               }
            }
         });

         datesGridPanel.add(dayCell);
      }

      int remainingCells = 42 - (startDayColumn + maxDaysInMonth);
      for (int i = 0; i < remainingCells; i++) {
         JPanel emptyCell = new JPanel();
         emptyCell.setBackground(colorCardWhite);
         emptyCell.setBorder(BorderFactory.createLineBorder(colorBorderLine, 1));
         datesGridPanel.add(emptyCell);
      }

      refreshBottomDetailViews();

      datesGridPanel.revalidate();
      datesGridPanel.repaint();
   }

   /**
    * 일정 배지 생성
    */
   private JButton createScheduleBadge(String text, Color color, String scheduleId, String dateStr) {
      String iconText = text;
      if (text.equals("주간근무")) {
         iconText = "주간";
      } else if (text.equals("야간근무")) {
         iconText = "야간";
      } else if (text.equals("휴무")) {
         iconText = "휴무";
      }

      JButton badgeBtn = new JButton(iconText);
      badgeBtn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
      badgeBtn.setOpaque(true);
      badgeBtn.setBackground(Color.WHITE);
      badgeBtn.setForeground(new Color(31, 41, 55));
      badgeBtn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, color),
            BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(243, 244, 246), 1, true),
                  new EmptyBorder(2, 6, 2, 6))));
      badgeBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));
      badgeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
      badgeBtn.setHorizontalAlignment(SwingConstants.LEFT);
      badgeBtn.setFocusPainted(false);

      badgeBtn.addActionListener(e -> {
         selectedDateString = dateStr;
         if (listener != null) {
            listener.onScheduleClicked(scheduleId, dateStr);
         }
      });

      return badgeBtn;
   }

   private JButton createMoreBadge(int hiddenCount, String dateStr) {
      JButton moreBtn = new JButton("+" + hiddenCount + "개 더");
      moreBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
      moreBtn.setOpaque(true);
      moreBtn.setBackground(new Color(249, 250, 251));
      moreBtn.setForeground(new Color(75, 85, 99));
      moreBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(2, 6, 2, 6)));
      moreBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 22));
      moreBtn.setHorizontalAlignment(SwingConstants.LEFT);
      moreBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
      moreBtn.setFocusPainted(false);
      moreBtn.addActionListener(e -> {
         selectedDateString = dateStr;
         if (listener != null) {
            listener.onDateSelected(dateStr);
         }
      });
      return moreBtn;
   }

   /**
    * 하단 상세 카드 영역을 새로고침합니다.
    *
    * 역할: - 첫 번째 카드는 오늘 일정 - 두 번째 카드는 현재 선택한 날짜의 일정 을 보여줍니다.
    *
    * 왜 필요한가? - currentDateString과 selectedDateString의 의미를 UI에서 분명하게 구분해 사용자 혼란을
    * 줄이기 위함입니다.
    */
   private void refreshBottomDetailViews() {
      bottomDetailPanel.removeAll();

      // 오늘 일정 카드
      bottomDetailPanel.add(createDailyDetailCard("오늘 일정", currentDateString));

      // 사용자가 현재 선택한 날짜의 일정 카드
      bottomDetailPanel.add(createDailyDetailCard("선택한 날짜 일정", selectedDateString));

      bottomDetailPanel.revalidate();
      bottomDetailPanel.repaint();
   }

   /**
    * 날짜별 상세 카드 하나를 생성합니다.
    *
    * 역할: - 카드 제목(예: 오늘 일정, 선택한 날짜 일정)을 표시합니다. - 해당 날짜의 일정 목록을 보여줍니다.
    *
    * @param titleText 카드 상단 제목
    * @param dateStr   표시할 날짜 문자열
    * @return 생성된 상세 카드 패널
    */
   private JPanel createDailyDetailCard(String titleText, String dateStr) {
      JPanel cardPanel = new JPanel(new BorderLayout());
      cardPanel.setBackground(Color.WHITE);
      cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true), new EmptyBorder(16, 18, 16, 18)));

      LocalDate date = LocalDate.parse(dateStr);

      // 카드 상단 제목
      JLabel titleLabel = new JLabel(titleText);
      titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
      titleLabel.setForeground(new Color(31, 41, 55));
      titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      leftPanel.setBackground(Color.WHITE);
      leftPanel.setPreferredSize(new Dimension(90, 80));

      JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
      dayLabel.setFont(new Font("맑은 고딕", Font.BOLD, 40));
      dayLabel.setForeground(new Color(37, 99, 235));
      dayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

      String weekText = convertDayOfWeekToKorean(date);

      JLabel weekLabel = new JLabel(weekText);
      weekLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
      weekLabel.setForeground(Color.GRAY);
      weekLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

      leftPanel.add(dayLabel);
      leftPanel.add(Box.createVerticalStrut(5));
      leftPanel.add(weekLabel);

      JPanel contentPanel = new JPanel();
      contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
      contentPanel.setBackground(Color.WHITE);

      boolean hasEvent = false;
      for (CalendarScheduleItem schedule : getSchedulesForDate(dateStr)) {
         hasEvent = true;

         // 일정 색상을 badgeColor로 표시 (신버전 반영)
         JLabel shiftLabel = new JLabel(
               "• " + schedule.getShiftType() + "  " + schedule.getStartTime() + " ~ " + schedule.getEndTime());
         shiftLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
         shiftLabel.setForeground(schedule.getBadgeColor());

         JLabel workplaceLabel = new JLabel("  근무지: " + schedule.getWorkplaceName());
         workplaceLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
         workplaceLabel.setForeground(new Color(107, 114, 128));

         contentPanel.add(shiftLabel);
         contentPanel.add(Box.createVerticalStrut(2));
         contentPanel.add(workplaceLabel);
         contentPanel.add(Box.createVerticalStrut(10));
      }

      if (!hasEvent) {
         JLabel empty = new JLabel("등록된 일정이 없습니다.");
         empty.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
         empty.setForeground(Color.GRAY);
         contentPanel.add(empty);
      }

      JScrollPane scrollPane = new JScrollPane(contentPanel);
      scrollPane.setBorder(null);
      scrollPane.getViewport().setBackground(Color.WHITE);
      scrollPane.setPreferredSize(new Dimension(220, 110));

      JPanel centerWrapper = new JPanel(new BorderLayout());
      centerWrapper.setBackground(Color.WHITE);
      centerWrapper.setBorder(new EmptyBorder(0, 20, 0, 0));
      centerWrapper.add(scrollPane, BorderLayout.CENTER);

      JPanel contentWrapper = new JPanel(new BorderLayout());
      contentWrapper.setBackground(Color.WHITE);
      contentWrapper.add(titleLabel, BorderLayout.NORTH);

      JPanel bodyPanel = new JPanel(new BorderLayout());
      bodyPanel.setBackground(Color.WHITE);
      bodyPanel.add(leftPanel, BorderLayout.WEST);
      bodyPanel.add(centerWrapper, BorderLayout.CENTER);

      contentWrapper.add(bodyPanel, BorderLayout.CENTER);
      cardPanel.add(contentWrapper, BorderLayout.CENTER);

      return cardPanel;
   }

   private List<CalendarScheduleItem> getSchedulesForDate(String dateStr) {
      List<CalendarScheduleItem> dailySchedules = new ArrayList<>();
      for (CalendarScheduleItem schedule : scheduleItems) {
         if (schedule.getDate().equals(dateStr)) {
            dailySchedules.add(schedule);
         }
      }
      return dailySchedules;
   }

   private String convertDayOfWeekToKorean(LocalDate date) {
      switch (date.getDayOfWeek()) {
      case MONDAY:
         return "월요일";
      case TUESDAY:
         return "화요일";
      case WEDNESDAY:
         return "수요일";
      case THURSDAY:
         return "목요일";
      case FRIDAY:
         return "금요일";
      case SATURDAY:
         return "토요일";
      case SUNDAY:
         return "일요일";
      default:
         return "";
      }
   }

}