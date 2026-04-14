package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import model.Shift;
import model.Workplace;

public class ShiftEditDialog extends JDialog {

    private static final Color COLOR_BG        = Color.WHITE; // 배경색 흰색으로 통일
    private static final Color COLOR_CARD      = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(25, 31, 40);
    private static final Color COLOR_TEXT_GRAY = new Color(139, 149, 161);
    private static final Color COLOR_BLUE      = new Color(49, 130, 246);
    private static final Color COLOR_RED       = new Color(230, 73, 128);
    private static final Color COLOR_BORDER    = new Color(229, 229, 234);
    private static final Font  FONT_TITLE      = new Font("맑은 고딕", Font.BOLD, 15);
    private static final Font  FONT_LABEL      = new Font("맑은 고딕", Font.BOLD, 12);
    private static final Font  FONT_INPUT      = new Font("맑은 고딕", Font.PLAIN, 13);

    private static class ModernButton extends JButton {
        public ModernButton(String text, Color bg, Color fg) {
            super(text);
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(fg); setBackground(bg);
            setFont(new Font("맑은 고딕", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g); g2.dispose();
        }
    }

    public static class ShiftFormData {
        private final String selectedWorkplaceText, shiftType, startTime, endTime;
        public ShiftFormData(String sel, String type, String start, String end) {
            this.selectedWorkplaceText = sel; this.shiftType = type;
            this.startTime = start; this.endTime = end;
        }
        public String getSelectedWorkplaceText() { return selectedWorkplaceText; }
        public String getShiftType()             { return shiftType; }
        public String getStartTime()             { return startTime; }
        public String getEndTime()               { return endTime; }
    }

    public interface OnSaveListener   { void onSave(ShiftFormData formData); }
    public interface OnDeleteListener { void onDelete(); }

    private final Frame            parentFrame;
    private final List<Workplace>  workplaceList;
    private final Shift            existingShift;
    private final String           dateStringParam;
    private final OnSaveListener   onSaveListener;
    private final OnDeleteListener onDeleteListener;

    private JTextField        dateInput;
    private JComboBox<String> workplaceDropdown;
    private JComboBox<String> typeDropdown;
    private JTextField        startInput;
    private JTextField        endInput;

    public ShiftEditDialog(Frame parentFrame, List<Workplace> workplaceList, Shift existingShift,
            String dateStringParam, OnSaveListener onSaveListener, OnDeleteListener onDeleteListener) {
        super(parentFrame, true);
        this.parentFrame = parentFrame; this.workplaceList = workplaceList;
        this.existingShift = existingShift; this.dateStringParam = dateStringParam;
        this.onSaveListener = onSaveListener; this.onDeleteListener = onDeleteListener;
        initializeDialog(); initializeUI(); fillInitialValues();
    }

    private void initializeDialog() {
        setTitle(existingShift == null ? "일정 등록" : "일정 수정/삭제");
        setSize(420, 460); // 사이즈 조정
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(COLOR_BG);
        outer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);

        // [1] 타이틀
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setBackground(COLOR_CARD);
        titleRow.setBorder(new EmptyBorder(20, 20, 0, 20));
        JLabel titleLbl = new JLabel(existingShift == null ? "일정 등록" : "일정 수정/삭제");
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(COLOR_TEXT_DARK);
        titleRow.add(titleLbl);

        // [2] 폼
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBackground(COLOR_CARD);
        form.setBorder(new EmptyBorder(10, 20, 15, 20)); // 하단 여백을 20에서 15로 살짝 줄임

        dateInput = new JTextField(dateStringParam);
        dateInput.setEditable(false);
        dateInput.setBackground(new Color(248, 249, 250));
        dateInput.setFont(FONT_INPUT);
        styleField(dateInput);

        workplaceDropdown = new JComboBox<>();
        workplaceDropdown.setFont(FONT_INPUT);
        styleDropdown(workplaceDropdown);
        if (workplaceList != null)
            for (Workplace wp : workplaceList)
                workplaceDropdown.addItem(buildWorkplaceDisplayText(wp));

        typeDropdown = new JComboBox<>(new String[]{"주간근무", "야간근무", "휴무"});
        typeDropdown.setFont(FONT_INPUT);
        styleDropdown(typeDropdown);
        typeDropdown.addActionListener(e -> updateTimeFieldsByShiftType());

        startInput = new JTextField(); startInput.setFont(FONT_INPUT); styleField(startInput);
        endInput   = new JTextField(); endInput.setFont(FONT_INPUT);   styleField(endInput);

        form.add(makeLabel("날짜"));      form.add(dateInput);
        form.add(makeLabel("근무지"));    form.add(workplaceDropdown);
        form.add(makeLabel("근무 유형")); form.add(typeDropdown);
        form.add(makeLabel("시작 시간")); form.add(startInput);
        form.add(makeLabel("종료 시간")); form.add(endInput);

        // [3] 버튼 - 높이와 여백 수정
        JPanel btnRow = new JPanel(new GridLayout(1, 0, 10, 0)); 
        btnRow.setBackground(COLOR_CARD);
        // 하단 여백을 25에서 15로 줄여서 전체적으로 컴팩트하게 변경
        btnRow.setBorder(new EmptyBorder(0, 20, 20, 20)); 

        ModernButton saveBtn   = new ModernButton("저장", COLOR_BLUE, Color.WHITE);
        ModernButton cancelBtn = new ModernButton("취소", new Color(233, 236, 239), COLOR_TEXT_DARK);
        ModernButton deleteBtn = new ModernButton("삭제", new Color(239, 68, 68), Color.WHITE);
        
        // 버튼 높이를 34 -> 28로 줄임 (취향에 따라 26~30 사이로 조절해보세요!)
        Dimension btnSize = new Dimension(0, 10);
        saveBtn.setPreferredSize(btnSize);
        cancelBtn.setPreferredSize(btnSize);
        deleteBtn.setPreferredSize(btnSize);
        
        saveBtn.addActionListener(e   -> handleSave());
        cancelBtn.addActionListener(e -> dispose());
        deleteBtn.addActionListener(e -> handleDelete());
        
        if (existingShift != null) btnRow.add(deleteBtn);
        btnRow.add(cancelBtn); 
        btnRow.add(saveBtn);

        card.add(titleRow);
        card.add(makeDivider());
        card.add(form);
        card.add(btnRow);

        outer.add(card, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

 // 디자인 통일감을 위해 같은 스타일을 사용하는 커스텀 확인 창
    private class CustomConfirmDialog extends JDialog {
        private boolean confirmed = false;

        public CustomConfirmDialog(Frame parent, String title, String message) {
            super(parent, title, true);
            setLayout(new BorderLayout());
            setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(25, 30, 20, 30));

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            titleLbl.setAlignmentX(CENTER_ALIGNMENT);

            JLabel msgLbl = new JLabel("<html><center>" + message + "</center></html>");
            msgLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            msgLbl.setForeground(COLOR_TEXT_GRAY);
            msgLbl.setAlignmentX(CENTER_ALIGNMENT);
            msgLbl.setBorder(new EmptyBorder(10, 0, 20, 0));

            JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
            btnRow.setBackground(Color.WHITE);
            btnRow.setMaximumSize(new Dimension(200, 35));

            ModernButton cancelBtn = new ModernButton("취소", new Color(242, 244, 246), COLOR_TEXT_DARK);
            ModernButton confirmBtn = new ModernButton("삭제", new Color(239, 68, 68), Color.WHITE);

            cancelBtn.addActionListener(e -> dispose());
            confirmBtn.addActionListener(e -> { confirmed = true; dispose(); });

            btnRow.add(cancelBtn);
            btnRow.add(confirmBtn);

            panel.add(titleLbl);
            panel.add(msgLbl);
            panel.add(btnRow);

            add(panel);
            pack();
            setLocationRelativeTo(parent);
        }

        public boolean isConfirmed() { return confirmed; }
    }
    
    // 구분선 좌우 여백도 맞추고 싶다면 이 부분을 수정
    private JPanel makeDivider() {
        JPanel d = new JPanel(new BorderLayout());
        d.setBackground(COLOR_CARD);
        // 구분선이 양옆 끝까지 가지 않고 입력창 라인에 맞추게 하려면 20, 20 여백 추가
        d.setBorder(new EmptyBorder(0, 20, 0, 20)); 
        
        JPanel line = new JPanel();
        line.setBackground(COLOR_BORDER);
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        
        d.add(line, BorderLayout.CENTER);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return d;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_LABEL); l.setForeground(COLOR_TEXT_GRAY); return l;
    }

    private void styleField(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
    }
    
    private void styleDropdown(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        // 드롭다운 버튼(화살표) 배경색을 흰색으로 맞춤 (UI 매니저 영향 방지)
        for (int i = 0; i < combo.getComponentCount(); i++) {
            if (combo.getComponent(i) instanceof JButton) {
                combo.getComponent(i).setBackground(Color.WHITE);
            }
        }
    }

    private void fillInitialValues() {
        String defaultType = "주간근무", defaultStart = "09:00", defaultEnd = "18:00";
        if (existingShift != null) {
            defaultType  = convertModelShiftTypeToUi(existingShift);
            defaultStart = existingShift.getStartTime().toLocalTime().toString();
            defaultEnd   = existingShift.getEndTime().toLocalTime().toString();
            if (existingShift.getWorkplace() != null)
                workplaceDropdown.setSelectedItem(buildWorkplaceDisplayText(existingShift.getWorkplace()));
        }
        typeDropdown.setSelectedItem(defaultType);
        updateTimeFieldsByShiftType();
        if (!"휴무".equals(defaultType)) { startInput.setText(defaultStart); endInput.setText(defaultEnd); }
    }

    private void updateTimeFieldsByShiftType() {
        String sel = typeDropdown.getSelectedItem() != null ? typeDropdown.getSelectedItem().toString() : "";
        if ("휴무".equals(sel)) {
            startInput.setText("00:00"); endInput.setText("00:01");
            startInput.setEditable(false); endInput.setEditable(false); return;
        }
        startInput.setEditable(true); endInput.setEditable(true);
        if ("주간근무".equals(sel))      { startInput.setText("09:00"); endInput.setText("18:00"); }
        else if ("야간근무".equals(sel)) { startInput.setText("22:00"); endInput.setText("06:00"); }
    }

    private void handleSave() {
        try {
            String wp    = workplaceDropdown.getSelectedItem() != null ? workplaceDropdown.getSelectedItem().toString() : "";
            String type  = typeDropdown.getSelectedItem()     != null ? typeDropdown.getSelectedItem().toString()     : "";
            String start = startInput.getText().trim();
            String end   = endInput.getText().trim();
            if (wp.isBlank())    throw new IllegalArgumentException("근무지를 선택해주세요.");
            if (type.isBlank())  throw new IllegalArgumentException("근무 유형을 선택해주세요.");
            if (start.isBlank()) throw new IllegalArgumentException("시작 시간을 입력해주세요.");
            if (end.isBlank())   throw new IllegalArgumentException("종료 시간을 입력해주세요.");
            validateTimeInputs(type, start, end);
            if (onSaveListener != null) onSaveListener.onSave(new ShiftFormData(wp, type, start, end));
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "일정 입력 처리 중 오류가 발생했습니다.");
        }
    }

    private boolean isValidTimeFormat(String t) {
        try { LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm")); return true; }
        catch (DateTimeParseException e) { return false; }
    }

    private void validateTimeInputs(String type, String start, String end) {
        if (!isValidTimeFormat(start)) throw new IllegalArgumentException("시작 시간은 HH:mm 형식으로 입력해주세요. 예: 09:00");
        if (!isValidTimeFormat(end))   throw new IllegalArgumentException("종료 시간은 HH:mm 형식으로 입력해주세요. 예: 18:00");
        if (!"휴무".equals(type) && start.equals(end)) throw new IllegalArgumentException("시작 시간과 종료 시간은 같을 수 없습니다.");
    }

    private void handleDelete() {
        // 커스텀 다이얼로그 생성 및 호출
        CustomConfirmDialog dialog = new CustomConfirmDialog(
            parentFrame, 
            "일정 삭제", 
            "이 일정을 삭제하시겠습니까?<br>삭제 후에는 복구할 수 없습니다."
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            if (onDeleteListener != null) onDeleteListener.onDelete();
            dispose();
        }
    }

    private String buildWorkplaceDisplayText(Workplace wp) {
        return String.format("%s (시급: %,d원)", wp.getCompanyName(), wp.getHourlyRate());
    }

    private String convertModelShiftTypeToUi(Shift shift) {
        switch (shift.getShiftType()) {
            case DAY:   return "주간근무";
            case NIGHT: return "야간근무";
            case OFF:   return "휴무";
            default: throw new IllegalArgumentException("지원하지 않는 ShiftType입니다.");
        }
    }

    public void showDialog() { setVisible(true); }
}