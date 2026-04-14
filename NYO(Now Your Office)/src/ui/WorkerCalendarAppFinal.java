package ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ui.MyPagePanel;
import ui.BoardPanel;
import ui.SidebarPanel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import model.Post;
import model.Shift;
import model.ShiftType;
import ui.TopNavigationBar;
import model.Workplace;
import service.HourlySalaryStrategy;
import service.OffSalaryStrategy;
import service.SalaryEngine;
import service.SalaryStrategy;
import service.ShiftManager;
import service.StatisticsService;
import service.StorageService;
import service.WorkplaceManager;
import ui.CalendarPanel.CalendarScheduleItem;
import controller.WorkplaceController;
import controller.ShiftController;

/**
 * 프로그램 전체를 연결하는 메인 프레임입니다.
 *
 * 초보 팀원을 위한 읽는 방법:
 * 1. 로그인/회원가입: createLoginPanel(), createSignupPanel()
 * 2. 메인 화면 조립: createDashboardPanel()
 * 3. 왼쪽 필터/달력 갱신: createSidebarPanel(), refreshCalendarView()
 * 4. 일정/근무지 저장 흐름: initializeControllers()
 *
 * 자세한 수정 포인트는 프로젝트 루트의 TEAM_CODE_GUIDE.md를 참고하면 됩니다.
 */
public class WorkerCalendarAppFinal extends JFrame {

   // =========================================================================
   // [1] 전역 변수 세팅 영역
   // =========================================================================
   private CardLayout cardLayout = new CardLayout();
   private JPanel mainContainer = new JPanel(cardLayout);

   // 💡 수업시간에 배운 컬렉션 프레임워크와 객체 직렬화(Serializable)를 활용하여 DB 대체
   private HashMap<String, User> userDatabase = new HashMap<>();
   private ArrayList<Post> boardDatabase = new ArrayList<>();
   private User loggedInUser;

   // 데이터를 물리적으로 저장할 로컬 파일명 (이 파일 지우면 데이터 초기화됨)
   private final String DATA_FILE = "nyo_database_v2.dat";

   // 날짜 관리 변수들
   private int currentYear = LocalDate.now().getYear();
   private int currentMonth = LocalDate.now().getMonthValue();
   private String currentDateString = LocalDate.now().toString();
   private String selectedDateString = LocalDate.now().toString();

   // 화면에 보여줄 UI 라벨들
   // monthTitleLabel, datesGridPanel, bottomDetailPaneltotalSalaryLabel,
   // netSalaryLabel, graphPanel 삭제 > 준혁
   private JPanel feedContainer;

   // 🎨 UI 색상 팔레트 (토스 앱 벤치마킹해서 직접 색상코드 추출함)
   private Color colorBackgroundGray = new Color(242, 244, 246);
   private Color colorCardWhite = Color.WHITE;
   private Color colorTextDark = new Color(25, 31, 40);
   private Color colorTextGray = new Color(139, 149, 161);
   private Color colorTossBlue = new Color(49, 130, 246);
   private Color colorAccentRed = new Color(230, 73, 128);
   private Color colorBorderLine = new Color(229, 229, 234);
   private Color colorPastelBlue = new Color(238, 244, 255);

   // 데스크톱 윈도우 알림을 띄우기 위한 객체
   private TrayIcon trayIcon;

   // 급여/통계 계산용 서비스
   private StatisticsService statisticsService;

   // 분리된 대시보드 패널
   private DashboardPanel dashboardPanel;

   // 메인 화면 카드들이 이미 생성되었는지 확인하는 플래그
   // 이유:
   // - initializeMainViews()가 여러 번 호출되면 같은 화면이 중복 add될 수 있음
   // - 안정화 단계에서는 화면 생성은 한 번만 하고 이후에는 refresh만 하는 편이 안전함
   private boolean mainViewsInitialized = false;

   // 분리된 달력 패널
   private CalendarPanel calendarPanel;

   // 분리된 마이페이지 패널
   // 이유:
   // - 마이페이지 UI를 WorkerCalendarAppFinal 밖으로 분리했기 때문
   // - 사용자 정보 갱신 시 setUserInfo(...) 호출에 사용
   private MyPagePanel myPagePanel;

   // 현재 선택된 근무지 필터
   // null이면 전체 보기 상태
   private String selectedWorkplaceFilterId = null;
   private static final String ALL_WORKPLACES_LABEL = "전체 근무지 보기";

   // 근무지 모듈화를 위한 변수
   private WorkplaceManager workplaceManager;

   // 근무지 기능 전용 컨트롤러
   private WorkplaceController workplaceController;

   // Shift 관련 순수 로직을 위임할 관리자 객체
   private ShiftManager shiftManager;

   // Shift 기능 전용 컨트롤러
   private ShiftController shiftController;

   // 분리된 게시판 패널
   // 이유:
   // - 게시판 UI를 WorkerCalendarAppFinal 밖으로 분리했기 때문
   // - 새 글 작성 후 refreshFeed()를 호출할 때 사용
   private BoardPanel boardPanel;

   // 상단 네비게이션 바
   // 이유:
   // - 메뉴 UI를 분리했기 때문에 외부에서 참조 필요
   private TopNavigationBar topNavigationBar;

// =========================================================================
// [백엔드 서비스 주입 영역]
// UI는 직접 계산하지 않고 서비스에 위임하기 위해 아래 객체를 유지함
// =========================================================================
   private SalaryEngine salaryEngine;
   // private StorageService storageService;

// 알림 중복 방지용
   private java.util.Set<String> notifiedShiftIds = new java.util.HashSet<>();

// Timer 충돌 방지를 피하기 위해 javax.swing.Timer를 명시적으로 사용
   private javax.swing.Timer alarmTimer;

   // SidebarPanel 인스턴스를 저장 (갱신용)
   private SidebarPanel sidebarPanel;

   // =========================================================================
   // [2] 데이터 모델 (DTO 역할) - 파일 저장을 위해 전부 Serializable 구현!
   // =========================================================================

   // 파일 하나에 세 가지 데이터를 한 번에 담아서 쓰기/읽기 하려고 만든 래퍼 클래스
   static class AppData implements Serializable {
      private static final long serialVersionUID = 1L;
      HashMap<String, User> userDB;
      ArrayList<Post> boardDB;
   }

   static class User implements Serializable {
      private static final long serialVersionUID = 1L;
      String id;
      String pw;
      String name;
      String phone;
      String email;
      String company;
      String contractType;
      int hourlyWage; // 기본 시급
      double taxRate; // 세금 공제율 (프리랜서 3.3% 등)

      // =========================
      // [백엔드 연동용 공식 데이터 구조]
      // UI에서 임시 Schedule을 쓰던 구조를
      // 점진적으로 model.Workplace / model.Shift 중심으로 바꾸기 위해 추가> 준혁
      // =========================

      List<Workplace> workplaces = new ArrayList<>();
      List<Shift> shifts = new ArrayList<>();

      public User(String id, String pw, String name, String phone, String email) {
         this.id = id;
         this.pw = pw;
         this.name = name;
         this.phone = phone;
         this.email = email;
         this.company = "미등록";
         this.contractType = "정규직";
         this.hourlyWage = 10030; // 2024년 최저시급 기준 초기화
         this.taxRate = 0.0;

      }
   }

   // 기본 JButton이 너무 안 예뻐서 둥근 모서리로 직접 다시 그린 커스텀 버튼 클래스
   static class ModernButton extends JButton {
      public ModernButton(String text, Color bgColor, Color fgColor) {
         super(text);
         setContentAreaFilled(false);
         setFocusPainted(false);
         setBorderPainted(false);
         setForeground(fgColor);
         setBackground(bgColor);
         setFont(new Font("맑은 고딕", Font.BOLD, 14));
         setCursor(new Cursor(Cursor.HAND_CURSOR));
      }

      @Override
      protected void paintComponent(Graphics g) {
         Graphics2D g2 = (Graphics2D) g.create();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 계단현상 방지용 안티앨리어싱
         g2.setColor(getBackground());
         g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // 모서리 둥글게 깎기
         super.paintComponent(g);
         g2.dispose();
      }
   }

   // =========================================================================
   // [3] 프로그램 시작점 (생성자 및 I/O 설정)
   // =========================================================================
   public WorkerCalendarAppFinal() {
      setTitle("NYO (Now Your Office) - 직장인 스마트 캘린더");
      setExtendedState(JFrame.MAXIMIZED_BOTH);
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      setLocationRelativeTo(null);
      addWindowListener(new java.awt.event.WindowAdapter() {
         @Override
         public void windowClosing(java.awt.event.WindowEvent e) {
            shutdownApplication();
         }
      });

      // 기존 로컬 데이터 로드
      loadDataFromFile();
      initializeDefaultUserIfNeeded();

      // 백엔드 서비스 초기화
      initializeBackendServices();

      // 컨트롤러 초기화
      initializeControllers();

      // 시스템 트레이 준비
      setupSystemTrayNotification();

      mainContainer.add(createLoginPanel(), "LOGIN");
      mainContainer.add(createSignupPanel(), "SIGNUP");

      add(mainContainer);
      cardLayout.show(mainContainer, "LOGIN");

      // 백그라운드 알림 타이머 시작
      startBackgroundNotificationTimer();
   }

   // 💡 DB 없이 자바 스트림(Stream)으로 데이터 영속성 유지하기!
   private void loadDataFromFile() {
      File file = new File(DATA_FILE);
      if (file.exists()) {
         try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            AppData data = (AppData) ois.readObject();
            this.userDatabase = data.userDB;
            // 레거시 scheduleDB도 함께 로드하지만,
            // 현재 실제 일정 원본은 loggedInUser.shifts 이다.
            // 로그인 성공 시 Shift 기준으로 다시 동기화한다. > 준혁
            this.boardDatabase = data.boardDB;

            ois.close();
            fis.close();
            System.out.println("[디버그] 기존 데이터 파일 로드 완료!");
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[디버그] 데이터 로드 중 에러 발생...");
         }
      } else {
         System.out.println("[디버그] 저장된 파일이 없어서 초기 더미 데이터를 생성합니다.");
         // 최초 실행용 테스트 계정 (아이디: test / 비번: 1234)
         User testUser = new User("test", "1234", "고양이", "010-1234-5678", "test@mail.com");
         testUser.company = "서울 수색 보안업체";
         testUser.hourlyWage = 12000;
         testUser.taxRate = 0.0932;
         userDatabase.put("test", testUser);
         saveDataToFile();
      }
   }

   private void saveDataToFile() {
      try {

         FileOutputStream fos = new FileOutputStream(DATA_FILE);
         ObjectOutputStream oos = new ObjectOutputStream(fos);

         AppData data = new AppData();
         data.userDB = this.userDatabase;
         data.boardDB = this.boardDatabase;

         oos.writeObject(data);

         oos.close();
         fos.close();
         System.out.println("[디버그] 로컬 파일에 데이터 저장 완료!");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void initializeDefaultUserIfNeeded() {
      if (userDatabase == null) {
         userDatabase = new HashMap<>();
      }
      if (boardDatabase == null) {
         boardDatabase = new ArrayList<>();
      }
      if (userDatabase.containsKey("test")) {
         return;
      }

      User testUser = new User("test", "1234", "테스트 사용자", "010-1234-5678", "test@mail.com");
      testUser.company = "기본 근무지";
      testUser.contractType = "아르바이트";
      testUser.hourlyWage = 12000;
      testUser.taxRate = 0.033;
      ensureUserCollections(testUser);
      userDatabase.put("test", testUser);
      saveDataToFile();
   }

   private void ensureUserCollections(User user) {
      if (user == null) {
         return;
      }
      if (user.workplaces == null) {
         user.workplaces = new ArrayList<>();
      }
      if (user.shifts == null) {
         user.shifts = new ArrayList<>();
      }
      mergeShiftWorkplacesIntoUser(user);
   }

   private void mergeShiftWorkplacesIntoUser(User user) {
      if (user == null || user.workplaces == null || user.shifts == null) {
         return;
      }

      for (Shift shift : user.shifts) {
         if (shift == null || shift.getWorkplace() == null) {
            continue;
         }

         String shiftWorkplaceId = shift.getWorkplace().getId();
         boolean exists = false;

         for (Workplace workplace : user.workplaces) {
            if (workplace != null && shiftWorkplaceId.equals(workplace.getId())) {
               exists = true;
               break;
            }
         }

         if (!exists) {
            user.workplaces.add(shift.getWorkplace());
         }
      }
   }

   // =========================================================================
   // [4] 로그인 및 회원가입 패널 구현
   // =========================================================================
   private JPanel createLoginPanel() {
      JPanel wrap = new JPanel(new GridBagLayout()); // 화면 정중앙에 배치하기 위해 사용
      wrap.setBackground(colorBackgroundGray);

      JPanel box = new JPanel(new GridLayout(6, 1, 10, 10));
      box.setBackground(colorCardWhite);
      box.setBorder(new EmptyBorder(50, 50, 50, 50));

      JLabel title = new JLabel("NYO Calendar", SwingConstants.CENTER);
      title.setFont(new Font("Arial", Font.BOLD, 28));
      title.setForeground(colorTossBlue);

      JTextField idTextField = new JTextField("test", 15);
      idTextField.setBorder(BorderFactory.createTitledBorder("아이디 (기본: test)"));
      JPasswordField passwordField = new JPasswordField("1234", 15);
      passwordField.setBorder(BorderFactory.createTitledBorder("비밀번호 (기본: 1234)"));

      ModernButton loginBtn = new ModernButton("로그인", colorTossBlue, colorCardWhite);
      ModernButton signupBtn = new ModernButton("회원가입", new Color(233, 236, 239), colorTextDark);

      // 로그인 검증 로직
      loginBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            String inputId = idTextField.getText().trim();
            String inputPw = new String(passwordField.getPassword());

            if (userDatabase == null) {
               userDatabase = new HashMap<>();
            }
            if ("test".equals(inputId) && "1234".equals(inputPw)) {
               initializeDefaultUserIfNeeded();
               User savedUser = userDatabase.get("test");
               ensureUserCollections(savedUser);
               saveDataToFile();
               loggedInUser = savedUser;
               initializeMainViews();
               cardLayout.show(mainContainer, "DASHBOARD");
               return;
            }
            if (userDatabase.isEmpty()) {
               initializeDefaultUserIfNeeded();
            }

            // HashMap의 Key(ID)가 존재하고, 해당 객체의 비밀번호가 일치하는지 확인
            if (userDatabase.containsKey(inputId)) {
               User savedUser = userDatabase.get(inputId);
               if (savedUser.pw.equals(inputPw)) {
                  ensureUserCollections(savedUser);
                  saveDataToFile();
                  loggedInUser = savedUser; // 세션 유지

                  initializeMainViews(); // 대시보드 화면 생성
                  cardLayout.show(mainContainer, "DASHBOARD"); // 화면 전환
                  System.out.println("[디버그] 로그인 성공: " + loggedInUser.name);
                  return;
               }
            }
            // 실패 시 알림창 띄우기
            JOptionPane.showMessageDialog(WorkerCalendarAppFinal.this, "아이디 또는 비밀번호를 확인해주세요.");
         }
      });

      signupBtn.addActionListener(e -> cardLayout.show(mainContainer, "SIGNUP"));

      box.add(title);
      box.add(idTextField);
      box.add(passwordField);
      box.add(new JLabel()); // 여백용 빈 라벨
      box.add(loginBtn);
      box.add(signupBtn);

      wrap.add(box);
      return wrap;
   }

   private JPanel createSignupPanel() {
      JPanel wrap = new JPanel(new GridBagLayout());
      wrap.setBackground(colorBackgroundGray);

      JPanel box = new JPanel(new GridLayout(7, 1, 10, 10));
      box.setBackground(colorCardWhite);
      box.setBorder(new EmptyBorder(60, 80, 60, 80));
      box.setPreferredSize(new Dimension(500, 600));

      JLabel title = new JLabel("회원가입", SwingConstants.CENTER);
      title.setFont(new Font("맑은 고딕", Font.BOLD, 24));
      box.add(title);

      JTextField idTextField = new JTextField();
      idTextField.setBorder(BorderFactory.createTitledBorder("ID"));

      JPasswordField pwTextField = new JPasswordField();
      pwTextField.setBorder(BorderFactory.createTitledBorder("Password"));

      JTextField nameTextField = new JTextField();
      nameTextField.setBorder(BorderFactory.createTitledBorder("이름"));

      JTextField phoneTextField = new JTextField();
      phoneTextField.setBorder(BorderFactory.createTitledBorder("연락처"));

      ModernButton registerBtn = new ModernButton("가입하기", colorTossBlue, colorCardWhite);
      ModernButton cancelBtn = new ModernButton("취소", Color.LIGHT_GRAY, colorTextDark);

      registerBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            // 입력받은 정보로 새로운 User 객체를 만들어서 HashMap에 넣기
            String newId = idTextField.getText();
            String newPw = new String(pwTextField.getPassword());
            String newName = nameTextField.getText();
            String newPhone = phoneTextField.getText();

            User newUser = new User(newId, newPw, newName, newPhone, "");
            userDatabase.put(newId, newUser);

            saveDataToFile(); // 가입 즉시 파일 저장하여 유실 방지

            JOptionPane.showMessageDialog(WorkerCalendarAppFinal.this, "가입 완료! 로그인 해주세요.");
            cardLayout.show(mainContainer, "LOGIN");
         }
      });

      cancelBtn.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

      box.add(idTextField);
      box.add(pwTextField);
      box.add(nameTextField);
      box.add(phoneTextField);
      box.add(registerBtn);
      box.add(cancelBtn);

      wrap.add(box);
      return wrap;
   }

   /**
    * 로그인 이후 필요한 메인 화면들을 초기화합니다.
    * 
    * 역할: - DASHBOARD / MYPAGE / BOARD 카드를 mainContainer에 등록합니다. - 같은 카드가 여러 번 중복
    * 등록되지 않도록 최초 1회만 생성합니다.
    * 
    * 왜 필요한가? - 로그인 성공 시, 마이페이지 저장 후 등 여러 지점에서 이 메서드가 호출될 수 있습니다. - 매번 add()를 반복하면
    * 화면 카드가 중복 생성되어 구조가 꼬일 수 있습니다.
    */
   private void initializeMainViews() {
      // 이미 메인 화면 카드가 생성된 경우에는 다시 만들지 않음
      if (mainViewsInitialized) {
         return;
      }

      mainContainer.add(createDashboardPanel(), "DASHBOARD");
      mainContainer.add(createMyPagePanel(), "MYPAGE");
      mainContainer.add(createBoardPanel(), "BOARD");

      // 최초 1회 생성 완료 표시
      mainViewsInitialized = true;
   }

   // =========================================================================
   // [5] 메인 대시보드 및 상단 네비게이션 바
   // =========================================================================
   private JPanel createDashboardPanel() {
      JPanel dashboard = new JPanel(new BorderLayout(16, 0));
      dashboard.setBackground(colorBackgroundGray);
      dashboard.setBorder(new EmptyBorder(12, 16, 16, 16));

      // 왼쪽 사이드바
      dashboard.add(createSidebarPanel(), BorderLayout.WEST);

      // 오른쪽 전체 영역
      JPanel rightContent = new JPanel(new BorderLayout(0, 10));
      rightContent.setBackground(colorBackgroundGray);
      rightContent.add(createQuickActionPanel(), BorderLayout.NORTH);

      // =========================
      // 2. 가운데 메인 영역
      // 달력 + 하단 대시보드 카드/그래프를
      // 세로로 배치하기 위한 래퍼 패널
      // =========================
      JPanel centerContent = new JPanel(new BorderLayout(0, 15));
      centerContent.setBackground(colorBackgroundGray);

      // =========================
      // 3. 달력 패널 생성
      // =========================
      calendarPanel = new CalendarPanel(currentYear, currentMonth, currentDateString, selectedDateString,
            colorBackgroundGray, colorCardWhite, colorTextDark, colorTossBlue, colorAccentRed, colorBorderLine,
            colorPastelBlue, new CalendarPanel.CalendarPanelListener() {
               @Override
               public void onPreviousMonth() {
                  currentMonth--;
                  refreshCalendarView();
               }

               @Override
               public void onNextMonth() {
                  currentMonth++;
                  refreshCalendarView();
               }

               @Override
               public void onDateSelected(String dateString) {
                  selectedDateString = dateString;
                  refreshCalendarView();
                  // 날짜 칸 클릭은 "새 일정 추가" 동작으로 고정합니다.
                  // 기존 일정 수정은 일정 배지 클릭으로만 진입하게 하여
                  // 같은 날짜에 여러 근무지를 함께 등록할 수 있게 합니다.
                  openShiftEditDialog(null, dateString);
               }

               @Override
               public void onScheduleClicked(String scheduleId, String dateString) {
                  selectedDateString = dateString;

                  Shift existingShift = findShiftById(scheduleId);

                  refreshCalendarView();
                  openShiftEditDialog(existingShift, dateString);
               }
            });

      centerContent.add(calendarPanel, BorderLayout.CENTER);

      // 오른쪽 영역 중앙에 메인 콘텐츠 부착
      rightContent.add(centerContent, BorderLayout.CENTER);

      dashboard.add(rightContent, BorderLayout.CENTER);

      // 최초 진입 시 데이터 반영
      refreshCalendarView();

      return dashboard;
   }

   private JPanel createQuickActionPanel() {
      JPanel quickActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
      quickActionPanel.setBackground(colorBackgroundGray);
      quickActionPanel.setBorder(new EmptyBorder(0, 0, 4, 0));

      ModernButton boardButton = new ModernButton("통합 게시판", Color.WHITE, new Color(31, 41, 55));
      ModernButton myPageButton = new ModernButton("마이페이지", Color.WHITE, new Color(31, 41, 55));
      ModernButton exportButton = new ModernButton("명세서 추출", new Color(15, 118, 110), colorCardWhite);
      //ModernButton autoPatternButton = new ModernButton("패턴 등록", new Color(234, 88, 12), colorCardWhite);
      ModernButton logoutButton = new ModernButton("로그아웃", new Color(239, 68, 68), colorCardWhite);

      boardButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(0, 4, 0, 4)));
      myPageButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(0, 4, 0, 4)));
      //autoPatternButton.addActionListener(e -> openAutoPatternDialog());
      exportButton.addActionListener(e -> executeSalaryExportToTextFile());
      boardButton.addActionListener(e -> cardLayout.show(mainContainer, "BOARD"));
      myPageButton.addActionListener(e -> {
         refreshMyPageUserInfo();
         cardLayout.show(mainContainer, "MYPAGE");
      });
      logoutButton.addActionListener(e -> {
         loggedInUser = null;
         selectedWorkplaceFilterId = null;
         cardLayout.show(mainContainer, "LOGIN");
      });

      quickActionPanel.add(boardButton);
      quickActionPanel.add(myPageButton);
      quickActionPanel.add(exportButton);
      //quickActionPanel.add(autoPatternButton);
      quickActionPanel.add(logoutButton);

      return quickActionPanel;
   }

   private JPanel createSidebarPanel() {

      List<String> categoryData = buildSidebarCategoryData();

      sidebarPanel = new SidebarPanel(colorBackgroundGray, colorCardWhite, colorTextDark, colorBorderLine,
            loggedInUser.name, categoryData, dashboardPanel.getSalaryCardPanel(),
            dashboardPanel.getGraphSectionPanel(), new SidebarPanel.SidebarListener() {

               @Override
               public void onAddWorkplace() {
                  workplaceController.openAddWorkplaceDialog();
               }

               @Override
               public void onEditWorkplace() {
                  workplaceController.openEditWorkplaceDialog();
               }

               @Override
               public void onDeleteWorkplace() {
                  workplaceController.openDeleteWorkplaceDialog();
               }

               @Override
               public void onCategorySelected(String value) {
                  if (value == null || value.isBlank()) {
                     selectedWorkplaceFilterId = null;
                     refreshCalendarView();
                     return;
                  }

                  if (value.equals(ALL_WORKPLACES_LABEL)) {
                     selectedWorkplaceFilterId = null;
                  } else {
                     Workplace wp = findWorkplaceByDisplayText(value);

                     if (wp == null) {
                        selectedWorkplaceFilterId = null;
                     } else {
                        selectedWorkplaceFilterId = wp.getId();
                     }
                  }

                  refreshCalendarView();
               }
            });

      return sidebarPanel;
   }

   /**
    * 근무지 목록을 다시 불러와 Sidebar에 반영
    */
   private void refreshSidebarWorkplaceList() {

      if (sidebarPanel == null)
         return;

      List<String> categoryData = buildSidebarCategoryData();

      sidebarPanel.updateCategoryData(categoryData, getSelectedSidebarCategoryText());
   }

   // ★ 제일 중요한 달력 새로고침 로직! 데이터가 바뀔 때마다 무조건 호출됨
   private void refreshCalendarView() {
      if (currentMonth > 12) {
         currentMonth = 1;
         currentYear++;
      }
      if (currentMonth < 1) {
         currentMonth = 12;
         currentYear--;
      }

      if (calendarPanel != null) {
         calendarPanel.refreshCalendar(currentYear, currentMonth, currentDateString, selectedDateString,
               convertShiftsToCalendarItems());
      }

      refreshDashboardPanel();
      refreshSidebarWorkplaceList();
   }

   private List<CalendarScheduleItem> convertShiftsToCalendarItems() {
      List<CalendarScheduleItem> items = new ArrayList<>();

      for (Shift shift : getFilteredShifts()) {
         String shiftTypeText = convertModelShiftTypeToUi(shift.getShiftType());
         String workplaceName = shift.getWorkplace() != null ? shift.getWorkplace().getCompanyName() : "근무지 없음";
         String dateText = shift.getStartTime().toLocalDate().toString();
         String startText = shift.getStartTime().toLocalTime().toString();
         String endText = shift.getEndTime().toLocalTime().toString();

         Color badgeColor = colorTossBlue;
         if (shift.getShiftType() == ShiftType.DAY) {
            badgeColor = colorTossBlue;
         } else if (shift.getShiftType() == ShiftType.NIGHT) {
            badgeColor = colorAccentRed;
         } else if (shift.getShiftType() == ShiftType.OFF) {
            badgeColor = new Color(32, 201, 151);
         }

         items.add(new CalendarScheduleItem(shift.getId(), dateText, shiftTypeText, workplaceName, startText,
               endText, badgeColor));
      }

      return items;
   }

   private List<Shift> getCurrentMonthShifts() {
      List<Shift> monthShifts = new ArrayList<>();

      for (Shift shift : getFilteredShifts()) {
         LocalDate shiftDate = shift.getStartTime().toLocalDate();

         if (shiftDate.getYear() == currentYear && shiftDate.getMonthValue() == currentMonth) {
            monthShifts.add(shift);
         }
      }

      return monthShifts;
   }

   // 분리된 DashboardPanel 새로고침
   // 역할: 현재 달 Shift 목록 전달, 현재 로그인 사용자의 세율 전달
   // DashboardPanel이 세전/세후 급여와 비율 그래프를 갱신하도록 요청 > 준혁
   private void refreshDashboardPanel() {
      if (dashboardPanel == null) {
         return;
      }

      double taxRate = 0.0;
      if (loggedInUser != null) {
         taxRate = loggedInUser.taxRate;
      }

      dashboardPanel.refresh(getCurrentMonthShifts(), taxRate);
   }

   /**
    * 현재 로그인 사용자의 Shift 목록에서 날짜 기준으로 Shift를 찾음
    * 
    * 실제 조회 로직은 ShiftManager에 위임
    * 
    * @param dateString 찾을 날짜 문자열
    * @return 찾은 Shift, 없으면 null
    */
   private Shift findShiftByDate(String dateString) {
      // 로그인 사용자가 없으면 찾을 수 없음
      if (loggedInUser == null) {
         return null;
      }

      return shiftManager.findShiftByDate(loggedInUser.shifts, dateString);
   }

   /**
    * 현재 로그인 사용자의 Shift 목록에서 ID로 Shift를 찾음
    * 
    * 실제 조회 로직은 ShiftManager에 위임
    * 
    * @param shiftId 찾을 Shift ID
    * @return 찾은 Shift, 없으면 null
    */
   private Shift findShiftById(String shiftId) {
      // 로그인 사용자가 없으면 찾을 수 없음
      if (loggedInUser == null) {
         return null;
      }

      return shiftManager.findShiftById(loggedInUser.shifts, shiftId);
   }

   /**
    * 일정 등록/수정 다이얼로그를 엽니다.
    * 
    * 역할: - 실제 저장/삭제 흐름은 ShiftController에 위임합니다. - WorkerCalendarAppFinal은 이제 호출만
    * 담당합니다.
    */
   private void openShiftEditDialog(Shift existingShift, String dateStringParam) {
      shiftController.openShiftEditDialog(existingShift, dateStringParam);
   }

   /**
    * 교대/반복 근무 패턴 자동 생성 다이얼로그입니다.
    *
    * 패턴을 "추가"하고 싶을 때 초보 팀원이 가장 먼저 볼 곳:
    * 1. 아래 patternComboBox의 문자열 목록에 새 패턴 추가
    * 2. 새 패턴 안에 주/야/휴무 외 다른 토큰을 썼다면
    *    ShiftController.convertPatternToUiShiftTypes(...)도 함께 수정
    * 3. 특정 토큰에 다른 기본 시간을 주고 싶다면
    *    ShiftController.buildShiftForPatternDay(...) 수정
    *
    * 예:
    * - "주-주-야-야-비-비" 추가/수정 -> 여기서 콤보박스 문자열 수정
    * - "오전-오후-휴무" 같은 새 토큰 추가 -> ShiftController도 같이 수정
    */
   private void openAutoPatternDialog() {
      JDialog dialog = new JDialog(this, "교대근무 패턴 자동 생성", true);
      dialog.setSize(350, 250);
      dialog.setLocationRelativeTo(this);
      dialog.setLayout(new BorderLayout());

      JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
      formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

      JTextField startDateInput = new JTextField(selectedDateString);
      // 패턴 종류를 추가/삭제하고 싶으면 아래 문자열 배열을 수정하면 됩니다.
      // 단, "주/야/비/휴" 외의 새 토큰을 쓰는 경우에는
      // ShiftController.convertPatternToUiShiftTypes(...)도 같이 수정해야 합니다.
      JComboBox<String> patternComboBox = new JComboBox<>(new String[] { "주-주-야-야-비-비", "주-야-비-휴", "주-주-휴-휴" });
      JTextField totalDaysInput = new JTextField("30");

      formPanel.add(new JLabel("시작 날짜 (YYYY-MM-DD):"));
      formPanel.add(startDateInput);
      formPanel.add(new JLabel("근무 패턴:"));
      formPanel.add(patternComboBox);
      formPanel.add(new JLabel("생성할 일수:"));
      formPanel.add(totalDaysInput);

      JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      ModernButton executeBtn = new ModernButton("자동 생성", colorTossBlue, colorCardWhite);

      executeBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               // 시작 날짜 파싱
               LocalDate startLocalDate = LocalDate.parse(startDateInput.getText(),
                     DateTimeFormatter.ISO_LOCAL_DATE);

               // 생성할 총 일수
               int runDays = Integer.parseInt(totalDaysInput.getText());

               // 선택한 패턴 문자열 분리
               String selectedString = patternComboBox.getSelectedItem().toString();
               String[] rawPatternArray = selectedString.split("-");

               // 패턴 문자열을 UI용 근무유형 문자열 배열로 변환
               String[] safePatternArray = shiftController.convertPatternToUiShiftTypes(rawPatternArray);

               // 현재 로그인 사용자의 기본 근무지 확보
               Workplace workplace = getOrCreateDefaultWorkplaceForLoggedInUser();

               // 생성된 Shift들을 임시로 모아둘 리스트
               List<Shift> generatedShifts = new ArrayList<>();

               // 요청한 일수만큼 반복 생성
               for (int i = 0; i < runDays; i++) {
                  LocalDate currentDate = startLocalDate.plusDays(i);

                  // 패턴 반복 적용
                  String generatedShiftType = safePatternArray[i % safePatternArray.length];

                  // 하루치 Shift 생성
                  Shift newShift = shiftController.buildShiftForPatternDay(workplace, currentDate,
                        generatedShiftType, System.currentTimeMillis() + i);

                  generatedShifts.add(newShift);
               }

               // 한 번에 추가 및 저장
               shiftController.appendPatternShiftsAndPersist(generatedShifts);

               JOptionPane.showMessageDialog(dialog, runDays + "일치 스케줄이 생성되었습니다!");
               dialog.dispose();

            } catch (Exception ex) {
               ex.printStackTrace();
               JOptionPane.showMessageDialog(dialog, "날짜 형식이나 입력값을 다시 확인해주세요 ㅠㅠ");
            }
         }
      });

      btnWrapper.add(executeBtn);
      dialog.add(formPanel, BorderLayout.CENTER);
      dialog.add(btnWrapper, BorderLayout.SOUTH);
      dialog.setVisible(true);
   }

   // 콤보박스에서 선택한 표시 문자열에 해당하는 Workplace를 찾습니다.
   private Workplace findWorkplaceByDisplayText(String displayText) {
      if (loggedInUser == null) {
         return null;
      }

      return workplaceManager.findWorkplaceByDisplayText(loggedInUser.workplaces, displayText);
   }

   private List<String> buildSidebarCategoryData() {
      ensureUserCollections(loggedInUser);
      List<String> categoryData = workplaceController.buildWorkplaceDisplayList();
      categoryData.add(0, ALL_WORKPLACES_LABEL);
      return categoryData;
   }

   private String getSelectedSidebarCategoryText() {
      if (selectedWorkplaceFilterId == null) {
         return ALL_WORKPLACES_LABEL;
      }

      if (loggedInUser == null || loggedInUser.workplaces == null) {
         return ALL_WORKPLACES_LABEL;
      }

      for (Workplace workplace : loggedInUser.workplaces) {
         if (workplace != null && selectedWorkplaceFilterId.equals(workplace.getId())) {
            return workplaceManager.buildWorkplaceDisplayText(workplace);
         }
      }

      return ALL_WORKPLACES_LABEL;
   }

   // 특정 Workplace가 현재 사용자의 Shift 목록에서 사용 중인지 확인합니다.
   // 사용 중이라면 삭제하면 안 됩니다.

   // =========================================================================
   // [9] 마이페이지 (사용자 정보 수정 및 세금 설정)
   // =========================================================================
   /**
    * 마이페이지 화면 패널을 생성합니다.
    * 
    * 역할: - 실제 마이페이지 UI는 MyPagePanel에 위임합니다. - WorkerCalendarAppFinal은 저장 처리와 화면
    * 전환만 연결합니다.
    * 
    * @return 마이페이지 패널
    */
   private JPanel createMyPagePanel() {
      myPagePanel = new MyPagePanel(colorBackgroundGray, colorCardWhite, colorTextDark, colorTossBlue,

            // 저장 버튼 클릭 시 실제 저장 처리
            formData -> handleMyPageSave(formData),

            // 메인으로 돌아가기
            () -> cardLayout.show(mainContainer, "DASHBOARD"));

      // 현재 로그인 사용자 정보 반영
      refreshMyPageUserInfo();

      return myPagePanel;
   }

   /**
    * 현재 로그인 사용자 정보를 마이페이지 화면에 반영합니다.
    * 
    * 역할: - myPagePanel이 존재할 때만 setUserInfo(...)를 호출합니다. - 로그인 사용자 정보가 바뀐 뒤 UI 동기화에
    * 사용합니다.
    */
   private void refreshMyPageUserInfo() {
      // 마이페이지 패널이 아직 없으면 종료
      if (myPagePanel == null) {
         return;
      }

      // 로그인 사용자가 없으면 종료
      if (loggedInUser == null) {
         return;
      }

      myPagePanel.setUserInfo(loggedInUser.company, loggedInUser.contractType, loggedInUser.hourlyWage,
            loggedInUser.taxRate);
   }

   /**
    * 마이페이지에서 전달된 입력값을 실제 사용자 정보에 반영하고 저장합니다.
    * 
    * 역할: - 회사명 / 계약 형태 / 시급 / 세금 정보를 사용자 객체에 반영합니다. - 기본 근무지 정보도 함께 동기화합니다. - 저장 후
    * 달력/대시보드도 새로고침합니다.
    * 
    * @param formData 마이페이지 입력 데이터
    */
   private void handleMyPageSave(MyPagePanel.MyPageFormData formData) {
      try {
         // 로그인 사용자 확인
         if (loggedInUser == null) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
         }

         // 입력값 반영
         loggedInUser.company = formData.getCompanyName();
         loggedInUser.contractType = formData.getContractType();
         loggedInUser.hourlyWage = Integer.parseInt(formData.getHourlyWageText());

         // 세금 선택값 반영
         int selectedIndex = formData.getTaxDropdownIndex();
         if (selectedIndex == 0) {
            loggedInUser.taxRate = 0.0;
         } else if (selectedIndex == 1) {
            loggedInUser.taxRate = 0.033;
         } else {
            loggedInUser.taxRate = 0.0932;
         }

         // 기본 근무지 정보 동기화
         syncDefaultWorkplaceFromUserInfo();

         // 저장
         saveDataToFile();

         // 화면 정보 다시 반영
         refreshMyPageUserInfo();
         refreshCalendarView();

         JOptionPane.showMessageDialog(this, "내 정보가 완벽하게 업데이트 되었습니다!");
         cardLayout.show(mainContainer, "DASHBOARD");

      } catch (NumberFormatException ex) {
         JOptionPane.showMessageDialog(this, "시급은 숫자로 입력해주세요.");
      } catch (IllegalStateException ex) {
         JOptionPane.showMessageDialog(this, ex.getMessage());
      }
   }

   // =========================================================================
   // [10] 통합 게시판 (토스 공지사항 스타일의 카드 피드 UI)
   // =========================================================================

   /**
    * 게시판 화면 패널을 생성합니다.
    * 
    * 역할: - 실제 게시판 UI는 BoardPanel에 위임합니다. - WorkerCalendarAppFinal은 게시글 저장과 화면 전환만
    * 연결합니다.
    * 
    * @return 게시판 패널
    */
   private JPanel createBoardPanel() {
      boardPanel = new BoardPanel(colorBackgroundGray, colorTextDark, colorTextGray, colorTossBlue, colorBorderLine,
            loggedInUser.name, boardDatabase,

            // 글쓰기 완료 시 실제 저장 처리
            newPost -> {
               // 최신 글이 맨 위에 보이도록 0번 위치에 추가
               boardDatabase.add(0, newPost);

               // 로컬 파일 저장
               saveDataToFile();

               // 게시판 화면 갱신
               boardPanel.refreshFeed();
            },

            // 메인 화면으로 돌아가기
            () -> cardLayout.show(mainContainer, "DASHBOARD"));

      return boardPanel;
   }

   /**
    * ShiftType enum을 UI 문자열로 변환
    * 
    * 실제 변환 로직은 ShiftManager에 위임
    * 
    * @param shiftType ShiftType enum
    * @return UI 문자열
    */
   private String convertModelShiftTypeToUi(ShiftType shiftType) {
      return shiftManager.convertModelShiftTypeToUi(shiftType);
   }

   /**
    * UI 입력값(날짜/시간/근무유형 문자열)로 공식 백엔드 모델 Shift를 생성합니다.
    */
   /**
    * UI 입력값으로 Shift 객체 생성
    * 
    * 실제 생성 로직은 ShiftManager에 위임
    * 
    * @param shiftId         Shift ID
    * @param workplace       근무지
    * @param dateString      날짜 문자열
    * @param startTimeString 시작 시간 문자열
    * @param endTimeString   종료 시간 문자열
    * @param uiShiftType     UI 근무유형 문자열
    * @return 생성된 Shift 객체
    */
   private Shift createShiftFromUiInput(String shiftId, Workplace workplace, String dateString, String startTimeString,
         String endTimeString, String uiShiftType) {
      return shiftManager.createShiftFromUiInput(shiftId, workplace, dateString, startTimeString, endTimeString,
            uiShiftType);
   }

   // 현재 로그인한 사용자의 기본 근무지를 반환합니다.
   // 규칙: 기본 Workplace가 없으면 생성. 이미 있으면 그대로 반환.
   // 주의: 최신 사용자 정보 반영이 필요할 때는 먼저 syncDefaultWorkplaceFromUserInfo()를 호출해야 합니다.
   private Workplace getOrCreateDefaultWorkplaceForLoggedInUser() {
      if (loggedInUser == null) {
         throw new IllegalStateException("로그인된 사용자가 없습니다.");
      }

      if (loggedInUser.workplaces == null) {
         loggedInUser.workplaces = new ArrayList<>();
      }

      return workplaceManager.getOrCreateDefaultWorkplace(loggedInUser.workplaces, loggedInUser.id,
            loggedInUser.company, loggedInUser.hourlyWage);
   }

   // 현재 로그인 사용자의 기본 Workplace를
   // 최신 사용자 정보(회사명, 시급) 기준으로 동기화합니다.

   // 원칙: workplaces가 비어 있으면 새로 생성
   // 이미 있으면 0번 기본 Workplace를 새 객체로 교체
   // 기존 Shift는 그대로 유지
   // 이후 새로 생성되는 Shift부터 최신 Workplace를 사용 >준혁

   private void syncDefaultWorkplaceFromUserInfo() {
      if (loggedInUser == null) {
         throw new IllegalStateException("로그인된 사용자가 없습니다.");
      }

      if (loggedInUser.workplaces == null) {
         loggedInUser.workplaces = new ArrayList<>();
      }

      workplaceManager.syncDefaultWorkplaceFromUserInfo(loggedInUser.workplaces, loggedInUser.id,
            loggedInUser.company, loggedInUser.hourlyWage);
   }

   /**
    * 현재 선택된 근무지 필터를 적용한 Shift 목록 반환
    * 
    * 실제 필터링 로직은 ShiftManager에 위임
    * 
    * @return 필터 적용된 Shift 목록
    */
   private List<Shift> getFilteredShifts() {
      // 로그인 사용자가 없으면 빈 리스트 반환
      if (loggedInUser == null) {
         return new ArrayList<>();
      }

      return shiftManager.getFilteredShifts(loggedInUser.shifts, selectedWorkplaceFilterId);
   }

   // =========================================================================
   // [11] 고급 기능 : 텍스트 파일 추출(I/O) & 바탕화면 시스템 트레이 알림 (Thread)
   // =========================================================================

   // 자바 FileWriter 써서 바탕화면 폴더에 영수증 포맷으로 텍스트 파일 떨궈주는 기능
   private void executeSalaryExportToTextFile() {
      String fileName = String.format("%04d년_%02d월_급여명세서.txt", currentYear, currentMonth);

      try {
         FileWriter fileWriter = new FileWriter(fileName);
         BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

         bufferedWriter.write("=========================================\n");
         bufferedWriter.write("               NYO 급여명세서\n");
         bufferedWriter.write("=========================================\n");
         bufferedWriter.write("소속: " + loggedInUser.company + "\n");
         bufferedWriter.write("성명: " + loggedInUser.name + " (" + loggedInUser.contractType + ")\n");
         bufferedWriter.write("대상월: " + currentYear + "년 " + currentMonth + "월\n");
         bufferedWriter.write("기본시급: " + loggedInUser.hourlyWage + "원\n");
         bufferedWriter.write("-----------------------------------------\n");
         bufferedWriter.write("[상세 근무 내역]\n");

         long sumGrossAmount = 0L;

         List<Shift> monthShifts = getCurrentMonthShifts();

         for (Shift shift : monthShifts) {
            if (shift.getShiftType() == ShiftType.OFF) {
               continue;
            }

            String dateText = shift.getStartTime().toLocalDate().toString();
            String shiftTypeText = convertModelShiftTypeToUi(shift.getShiftType());
            String startText = shift.getStartTime().toLocalTime().toString();
            String endText = shift.getEndTime().toLocalTime().toString();
            long calculatedAmount = salaryEngine.calculateSalary(shift);

            bufferedWriter.write(String.format("- %s [%s] %s~%s : %,d원\n", dateText, shiftTypeText, startText,
                  endText, calculatedAmount));

            sumGrossAmount += calculatedAmount;
         }

         bufferedWriter.write("-----------------------------------------\n");

         long taxDeductionAmount = Math.round(sumGrossAmount * loggedInUser.taxRate);
         long realIncomeAmount = sumGrossAmount - taxDeductionAmount;

         bufferedWriter.write(String.format("총 세전 급여: %,d 원\n", sumGrossAmount));
         bufferedWriter.write(
               String.format("세금 공제금액: %,d 원 (공제율: %.2f%%)\n", taxDeductionAmount, loggedInUser.taxRate * 100));
         bufferedWriter.write("=========================================\n");
         bufferedWriter.write(String.format("실 수 령 액: %,d 원\n", realIncomeAmount));
         bufferedWriter.write("=========================================\n");

         bufferedWriter.close();
         fileWriter.close();

         JOptionPane.showMessageDialog(this, "프로젝트 폴더 안에 '" + fileName + "' 파일이 성공적으로 뽑혔습니다!");
         System.out.println("[디버그] 명세서 txt 파일 출력 성공!");

      } catch (IOException e) {
         e.printStackTrace();
         JOptionPane.showMessageDialog(this, "아앗... 파일 추출 중에 에러가 났습니다 ㅠㅠ");
      }
   }

   // 모니터 화면 오른쪽 아래 시계 쪽에 백그라운드로 프로그램 숨겨두는 윈도우 OS 트레이 기능
   private void setupSystemTrayNotification() {
      if (SystemTray.isSupported()) {
         try {
            // 우리가 따로 아이콘 이미지가 없어서 코드로 투명한 1x1 이미지를 강제로 그려서 넣어줌
            Image invisibleIcon = new java.awt.image.BufferedImage(1, 1,
                  java.awt.image.BufferedImage.TYPE_INT_ARGB);
            trayIcon = new TrayIcon(invisibleIcon, "NYO 백그라운드 알림기기");
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
            System.out.println("[디버그] 윈도우 시스템 트레이 등록 완료");
         } catch (AWTException e) {
            System.out.println("[디버그] 트레이 아이콘 등록 실패");
            e.printStackTrace();
         }
      } else {
         System.out.println("[디버그] 이 운영체제는 SystemTray를 지원 안함.");
      }
   }

   // 안드로이드 푸시알림처럼 백그라운드 스레드가 30초마다 돌면서 근무시간 다가오는지 감시하는 거!
   private void startBackgroundNotificationTimer() {
      // Timer 충돌 방지를 위해 javax.swing.Timer를 명시적으로 사용
      alarmTimer = new javax.swing.Timer(30000, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            // 로그인 안 되어 있거나 트레이가 준비되지 않았으면 중단
            if (loggedInUser == null || trayIcon == null) {
               return;
            }

            // Shift 목록이 없으면 검사할 데이터가 없음
            if (loggedInUser.shifts == null || loggedInUser.shifts.isEmpty()) {
               return;
            }

            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            // 이제부터는 Schedule이 아니라 Shift를 기준으로 검사
            for (Shift shift : loggedInUser.shifts) {
               try {
                  // 휴무는 알림 대상 아님
                  if (shift.getShiftType() == ShiftType.OFF) {
                     continue;
                  }

                  // 오늘 시작하는 근무만 검사
                  if (!shift.getStartTime().toLocalDate().equals(today)) {
                     continue;
                  }

                  // 이미 알림 보낸 Shift면 건너뜀
                  if (notifiedShiftIds.contains(shift.getId())) {
                     continue;
                  }

                  long minutesLeft = ChronoUnit.MINUTES.between(now, shift.getStartTime());

                  // 출근 30분 전~직전까지만 알림
                  if (minutesLeft >= 0 && minutesLeft <= 30) {
                     String shiftLabel = convertModelShiftTypeToUi(shift.getShiftType());

                     trayIcon.displayMessage("🔔 NYO 출근 알림",
                           "오늘 " + shiftLabel + " 시작까지 약 " + minutesLeft + "분 남았습니다! 준비하세요!",
                           TrayIcon.MessageType.INFO);

                     // 중복 알림 방지
                     notifiedShiftIds.add(shift.getId());

                     System.out.println("[디버그] Shift 알림 전송 완료: " + shift.getId());
                     System.out.println("현재 Shift 개수: " + loggedInUser.shifts.size());
                     System.out.println("이번 달 Shift 개수: " + getCurrentMonthShifts().size());
                  }
               } catch (Exception ex) {
                  ex.printStackTrace();
               }
            }
         }
      });

      alarmTimer.setInitialDelay(5000);
      alarmTimer.start();
   }

   private void shutdownApplication() {
      try {
         if (alarmTimer != null) {
            alarmTimer.stop();
         }
         if (trayIcon != null && SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(trayIcon);
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      } finally {
         dispose();
         System.exit(0);
      }
   }

   // 백엔드 서비스 초기화
   // 역할: - ShiftType별 급여 계산 전략 맵 구성 - SalaryEngine 생성 - StatisticsService 생성 -
   // StorageService 생성
   // UI에서는 앞으로 급여 계산/통계/CSV 저장을 직접 구현하지 않고 이 서비스 객체들을 사용해야 함

   private void initializeBackendServices() {
      // ShiftType별 급여 전략을 담을 맵
      Map<ShiftType, SalaryStrategy> strategyMap = new HashMap<>();

      // DAY / NIGHT는 시급 기반 계산
      strategyMap.put(ShiftType.DAY, new HourlySalaryStrategy());
      strategyMap.put(ShiftType.NIGHT, new HourlySalaryStrategy());

      // OFF는 0원 처리
      strategyMap.put(ShiftType.OFF, new OffSalaryStrategy());

      // 급여 엔진 생성
      salaryEngine = new SalaryEngine(strategyMap);

      // 통계 서비스 생성
      statisticsService = new StatisticsService(salaryEngine);

      // 저장 서비스 생성
      // 현재 앱은 직렬화(.dat) 저장 방식을 사용 중이므로 아직 연결하지 않음
      // 추후 CSV 저장 구조로 전환할 때 StorageService를 연결할 예정
      // storageService = new StorageService();

      // 근무지 로직 관리자 생성
      workplaceManager = new WorkplaceManager();

      // Shift 로직 관리자 생성
      shiftManager = new ShiftManager();

      // 대시보드 패널 생성
      dashboardPanel = new DashboardPanel(statisticsService);
   }

   /**
    * 컨트롤러 초기화 역할: - 근무지 관련 흐름을 WorkerCalendarAppFinal 밖으로 분리하기 위해
    * WorkplaceController를 생성합니다. 주의: - model.Workplace를 새로 만드는 것이 아니라, 기존
    * Workplace 모델을 사용하는 흐름 제어 객체를 만드는 것입니다.
    */
   /**
    * 컨트롤러 초기화
    * 
    * 역할: - 메인 클래스에 몰려 있는 근무지/일정 처리 로직을 각 컨트롤러로 분리하기 위해 생성한다.
    */
   private void initializeControllers() {
      workplaceController = new WorkplaceController(this, workplaceManager,

            // 현재 로그인 사용자 데이터 제공
            () -> {
               if (loggedInUser == null) {
                  return null;
               }

               return new WorkplaceController.WorkplaceUserContext(loggedInUser.id, loggedInUser.workplaces,
                     loggedInUser.shifts);
            },

            // 저장 처리
            () -> saveDataToFile(),

            // 화면 갱신 처리
            // 화면 갱신 처리
            // 역할:
            // - 근무지 추가/수정/삭제가 끝난 뒤
            //   왼쪽 사이드바 목록을 다시 그리고
            //   달력/급여/그래프도 다시 계산해서 보여줍니다.
            () -> {
               refreshSidebarWorkplaceList();
               refreshCalendarView();
            });

      shiftController = new ShiftController(this, shiftManager, workplaceManager,

            // 현재 로그인 사용자 데이터 제공
            () -> {
               if (loggedInUser == null) {
                  return null;
               }

               return new ShiftController.ShiftUserContext(loggedInUser.id, loggedInUser.workplaces,
                     loggedInUser.shifts);
            },

            // 저장 처리
            () -> saveDataToFile(),

            // 화면 갱신 처리
            () -> refreshCalendarView());
   }

   // =========================================================================
   // 메인(Main) 실행 함수
   // =========================================================================
   public static void main(String[] args) {
      // GUI 프로그램은 메인 스레드랑 별개로 이벤트 스레드(EDT)에서 실행해야 안 터짐!
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            WorkerCalendarAppFinal app = new WorkerCalendarAppFinal();
            app.setVisible(true);
         }
      });

   }
}
