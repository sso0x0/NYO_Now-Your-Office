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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class AddWorkplaceDialog extends JDialog {

    private static final Color COLOR_BG        = Color.WHITE; // 배경색 흰색으로 통일
    private static final Color COLOR_CARD      = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(25, 31, 40);
    private static final Color COLOR_TEXT_GRAY = new Color(139, 149, 161);
    private static final Color COLOR_BLUE      = new Color(49, 130, 246);
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

    public static class AddWorkplaceFormData {
        private final String companyName, hourlyRateText, dayHoursText, nightHoursText;
        public AddWorkplaceFormData(String c, String r, String d, String n) {
            companyName = c; hourlyRateText = r; dayHoursText = d; nightHoursText = n;
        }
        public String getCompanyName()    { return companyName; }
        public String getHourlyRateText() { return hourlyRateText; }
        public String getDayHoursText()   { return dayHoursText; }
        public String getNightHoursText() { return nightHoursText; }
    }

    public interface OnSaveListener { void onSave(AddWorkplaceFormData formData); }

    private final Frame          parentFrame;
    private final OnSaveListener onSaveListener;
    private JTextField companyInput, hourlyRateInput, dayHoursInput, nightHoursInput;

    public AddWorkplaceDialog(Frame parentFrame, OnSaveListener onSaveListener) {
        super(parentFrame, true);
        this.parentFrame = parentFrame; this.onSaveListener = onSaveListener;
        initializeDialog(); initializeUI();
    }

    private void initializeDialog() {
        setTitle("근무지 추가");
        setSize(380, 400); // 사이즈 조정
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(COLOR_BG);
        outer.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_CARD);
        // 카드형 테두리 선 제거로 완전한 흰색 배경 연출

        // [1] 타이틀
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 14));
        titleRow.setBackground(COLOR_CARD);
        JLabel titleLbl = new JLabel("근무지 추가");
        titleLbl.setFont(FONT_TITLE); titleLbl.setForeground(COLOR_TEXT_DARK);
        titleRow.add(titleLbl);

        // [2] 폼
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBackground(COLOR_CARD);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        companyInput    = styledField("");
        hourlyRateInput = styledField("");
        dayHoursInput   = styledField("8");
        nightHoursInput = styledField("8");

        form.add(makeLabel("근무지 이름"));           form.add(companyInput);
        form.add(makeLabel("시급 (원)"));             form.add(hourlyRateInput);
        form.add(makeLabel("기본 주간 근무시간 (h)")); form.add(dayHoursInput);
        form.add(makeLabel("기본 야간 근무시간 (h)")); form.add(nightHoursInput);

        // [3] 버튼
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnRow.setBackground(COLOR_CARD);
        ModernButton saveBtn   = new ModernButton("추가", COLOR_BLUE, Color.WHITE);
        ModernButton cancelBtn = new ModernButton("취소", new Color(233, 236, 239), COLOR_TEXT_DARK);
        saveBtn.setPreferredSize(new Dimension(88, 34));
        cancelBtn.setPreferredSize(new Dimension(88, 34));
        saveBtn.addActionListener(e   -> handleSave());
        cancelBtn.addActionListener(e -> dispose());
        btnRow.add(cancelBtn); btnRow.add(saveBtn);

        card.add(titleRow);
        card.add(makeDivider());
        card.add(form);
        // card.add(makeDivider()); // 버튼 위 실선 제거
        card.add(btnRow);

        outer.add(card, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

    private JPanel makeDivider() {
        JPanel d = new JPanel(); d.setBackground(COLOR_CARD);
        d.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); return d;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_LABEL); l.setForeground(COLOR_TEXT_GRAY); return l;
    }

    private JTextField styledField(String val) {
        JTextField f = new JTextField(val); f.setFont(FONT_INPUT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true), new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private void handleSave() {
        try {
            String c = companyInput.getText().trim(), r = hourlyRateInput.getText().trim();
            String d = dayHoursInput.getText().trim(), n = nightHoursInput.getText().trim();
            if (c.isBlank()) throw new IllegalArgumentException("근무지 이름을 입력해주세요.");
            if (r.isBlank()) throw new IllegalArgumentException("시급을 입력해주세요.");
            if (d.isBlank()) throw new IllegalArgumentException("기본 주간 근무시간을 입력해주세요.");
            if (n.isBlank()) throw new IllegalArgumentException("기본 야간 근무시간을 입력해주세요.");
            if (onSaveListener != null) onSaveListener.onSave(new AddWorkplaceFormData(c, r, d, n));
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "근무지 입력 처리 중 오류가 발생했습니다.");
        }
    }

    public void showDialog() { setVisible(true); }
}