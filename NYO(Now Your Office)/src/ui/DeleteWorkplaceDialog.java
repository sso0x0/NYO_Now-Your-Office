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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class DeleteWorkplaceDialog extends JDialog {
//test
    private static final Color COLOR_BG        = Color.WHITE;
    private static final Color COLOR_CARD      = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(25, 31, 40);
    private static final Color COLOR_TEXT_GRAY = new Color(139, 149, 161);
    private static final Color COLOR_RED       = new Color(239, 68, 68);
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

    public interface OnDeleteListener { void onDelete(String selectedWorkplaceText); }

    private final Frame            parentFrame;
    private final List<String>     workplaceDisplayList;
    private final OnDeleteListener onDeleteListener;
    private JComboBox<String>      workplaceDropdown;

    public DeleteWorkplaceDialog(Frame parentFrame, List<String> workplaceDisplayList, OnDeleteListener onDeleteListener) {
        super(parentFrame, true);
        this.parentFrame = parentFrame; this.workplaceDisplayList = workplaceDisplayList;
        this.onDeleteListener = onDeleteListener;
        initializeDialog(); initializeUI();
    }

    private void initializeDialog() {
        setTitle("근무지 삭제");
        setSize(400, 320); 
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

        // [1] 타이틀 및 가이드 문구 (왼쪽 정렬 레이아웃으로 변경)
        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.Y_AXIS));
        titleRow.setBackground(COLOR_CARD);
        // 왼쪽 여백 20을 주어 입력창 라인과 맞춤
        titleRow.setBorder(new EmptyBorder(20, 20, 15, 20)); 

        JLabel titleLbl = new JLabel("근무지 삭제");
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(COLOR_TEXT_DARK);
        titleLbl.setAlignmentX(LEFT_ALIGNMENT); // BoxLayout 내 왼쪽 정렬
        
        JLabel guideLbl = new JLabel("삭제할 근무지를 선택해주세요.");
        guideLbl.setFont(FONT_LABEL);
        guideLbl.setForeground(COLOR_TEXT_GRAY);
        guideLbl.setBorder(new EmptyBorder(6, 0, 0, 0));
        guideLbl.setAlignmentX(LEFT_ALIGNMENT); // BoxLayout 내 왼쪽 정렬

        titleRow.add(titleLbl);
        titleRow.add(guideLbl);

        // [2] 폼 (드롭다운 테두리 수정)
        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(COLOR_CARD);
        form.setBorder(new EmptyBorder(25, 20, 25, 20));

        // 드롭다운을 감싸는 패널에 테두리를 적용 (이중 테두리 방지)
        JPanel comboWrapper = new JPanel(new BorderLayout());
        comboWrapper.setBackground(Color.WHITE);
        comboWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)));

        workplaceDropdown = new JComboBox<>();
        workplaceDropdown.setFont(FONT_INPUT);
        workplaceDropdown.setBackground(Color.WHITE);
        workplaceDropdown.setBorder(null); // JComboBox 자체 테두리 제거
        workplaceDropdown.setFocusable(false);
        
        // 화살표 버튼 배경색 처리
        for (int i = 0; i < workplaceDropdown.getComponentCount(); i++) {
            if (workplaceDropdown.getComponent(i) instanceof JButton) {
                workplaceDropdown.getComponent(i).setBackground(Color.WHITE);
            }
        }
        
        if (workplaceDisplayList != null)
            for (String item : workplaceDisplayList) workplaceDropdown.addItem(item);

        comboWrapper.add(workplaceDropdown, BorderLayout.CENTER);
        form.add(comboWrapper, BorderLayout.CENTER);

        // [3] 하단 버튼
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0)); 
        btnRow.setBackground(COLOR_CARD);
        btnRow.setBorder(new EmptyBorder(0, 20, 20, 20)); 

        ModernButton cancelBtn = new ModernButton("취소", new Color(242, 244, 246), COLOR_TEXT_DARK);
        ModernButton deleteBtn = new ModernButton("삭제", COLOR_RED, Color.WHITE);
        
        Dimension btnSize = new Dimension(0, 36);
        cancelBtn.setPreferredSize(btnSize);
        deleteBtn.setPreferredSize(btnSize);
        
        cancelBtn.addActionListener(e -> dispose());
        deleteBtn.addActionListener(e -> handleDelete());
        
        btnRow.add(cancelBtn); 
        btnRow.add(deleteBtn);

        // 레이아웃 조립
        card.add(titleRow);
        card.add(makeDivider());
        card.add(form);
        card.add(btnRow);

        outer.add(card, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }
    private JPanel makeDivider() {
        JPanel d = new JPanel(new BorderLayout());
        d.setBackground(COLOR_CARD);
        d.setBorder(new EmptyBorder(0, 20, 0, 20)); 
        
        JPanel line = new JPanel();
        line.setBackground(COLOR_BORDER);
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        
        d.add(line, BorderLayout.CENTER);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return d;
    }

    private void styleDropdown(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        // ShiftEditDialog와 동일한 스타일의 보더 적용
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        
        for (int i = 0; i < combo.getComponentCount(); i++) {
            if (combo.getComponent(i) instanceof JButton) {
                combo.getComponent(i).setBackground(Color.WHITE);
            }
        }
    }

    private void handleDelete() {
        try {
            String sel = workplaceDropdown.getSelectedItem() != null
                    ? workplaceDropdown.getSelectedItem().toString() : "";
            if (sel.isBlank()) throw new IllegalArgumentException("삭제할 근무지를 선택해주세요.");
            
            // 기존 JOptionPane 대신 커스텀 느낌의 메시지 박스 활용 가능 (생략 시 기본 사용)
            int confirm = JOptionPane.showConfirmDialog(this, 
                "선택한 근무지를 삭제하시겠습니까?\n해당 근무지의 모든 데이터가 삭제됩니다.", 
                "삭제 확인", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (onDeleteListener != null) onDeleteListener.onDelete(sel);
                dispose();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "오류가 발생했습니다.");
        }
    }

    public void showDialog() { setVisible(true); }
}