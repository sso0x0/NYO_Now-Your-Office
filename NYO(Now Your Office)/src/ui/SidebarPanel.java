package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * 왼쪽 사이드바 전용 패널입니다.
 *
 * 이 패널은 "화면 조립"만 담당합니다.
 * 실제 근무지 추가/수정/삭제나 필터 적용은 여기서 직접 하지 않고,
 * 버튼/목록 선택 이벤트를 바깥(WorkerCalendarAppFinal)으로 전달합니다.
 */
public class SidebarPanel extends JPanel {

    /**
     * 사이드바 내부 버튼/리스트 이벤트를 외부로 전달하기 위한 인터페이스입니다.
     */
    public interface SidebarListener {
        void onAddWorkplace();
        void onEditWorkplace();
        void onDeleteWorkplace();
        void onCategorySelected(String value);
    }

    private final DefaultListModel<String> categoryModel;
    private final JList<String> categoryList;
    private boolean updatingCategoryData;
    private final JLabel filterTitleLabel;
    private final SidebarListener listener;

    /**
     * 사이드바를 구성합니다.
     */
    public SidebarPanel(
            Color background,
            Color cardWhite,
            Color textDark,
            Color borderColor,
            String userName,
            List<String> categoryData,
            JPanel salaryCardPanel,
            JPanel graphSectionPanel,
            SidebarListener listener) {

        this.listener = listener;
        this.categoryModel = new DefaultListModel<>();
        this.categoryList = new JList<>(categoryModel);

        setLayout(new BorderLayout());
        setBackground(background);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // 메인 컨텐츠 컨테이너 (세로 배치)
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(background);
        content.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 1. 프로필 카드
        JPanel profileCard = createCardPanel();
        JLabel welcome = new JLabel(userName + "님");
        welcome.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitle = new JLabel("이번 달 근무와 급여를 한눈에 확인하세요");
        subtitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        subtitle.setForeground(new Color(107, 114, 128));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        profileCard.add(welcome);
        profileCard.add(Box.createVerticalStrut(4));
        profileCard.add(subtitle);
        content.add(profileCard);
        content.add(Box.createVerticalStrut(12));

        // 2. 급여 요약 카드 (Dashboard에서 전달받음)
        if (salaryCardPanel != null) {
            salaryCardPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(salaryCardPanel);
            content.add(Box.createVerticalStrut(12));
        }

        // 3. 근무지 필터 카드
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        categoryList.setFixedCellHeight(34);
        categoryList.setBackground(Color.WHITE);
        categoryList.setForeground(textDark);
        categoryList.setSelectionBackground(new Color(239, 246, 255));
        categoryList.setSelectionForeground(new Color(30, 64, 175));
        categoryList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !updatingCategoryData && listener != null) {
                listener.onCategorySelected(categoryList.getSelectedValue());
            }
        });

        updateCategoryData(categoryData);

        JPanel filterCard = createCardPanel();
        filterTitleLabel = new JLabel("근무지 필터");
        filterTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        filterTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel filterSubtitle = new JLabel("선택한 근무지만 캘린더에 표시됩니다");
        filterSubtitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        filterSubtitle.setForeground(new Color(107, 114, 128));
        filterSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        filterCard.add(filterTitleLabel);
        filterCard.add(Box.createVerticalStrut(4));
        filterCard.add(filterSubtitle);
        filterCard.add(Box.createVerticalStrut(10));

        JScrollPane scroll = new JScrollPane(categoryList);
        scroll.setMaximumSize(new Dimension(250, 170));
        scroll.setPreferredSize(new Dimension(250, 170));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(new LineBorder(new Color(229, 231, 235)));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        
        filterCard.add(scroll);
        content.add(filterCard);
        content.add(Box.createVerticalStrut(12));

        // 4. 근무지 관리 카드 (버튼 3종)
        JButton addBtn = new JButton("추가");
        JButton editBtn = new JButton("수정");
        JButton deleteBtn = new JButton("삭제");

        styleActionButton(addBtn, new Color(37, 99, 235), Color.WHITE);
        styleActionButton(editBtn, Color.WHITE, new Color(55, 65, 81));
        styleActionButton(deleteBtn, Color.WHITE, new Color(220, 38, 38));

        addBtn.addActionListener(e -> listener.onAddWorkplace());
        editBtn.addActionListener(e -> listener.onEditWorkplace());
        deleteBtn.addActionListener(e -> listener.onDeleteWorkplace());

        JPanel actionCard = createCardPanel();
        JLabel actionTitle = new JLabel("근무지 관리");
        actionTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        actionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        actionCard.add(actionTitle);
        actionCard.add(Box.createVerticalStrut(10));

        JPanel actionRow = new JPanel(new GridLayout(1, 3, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(deleteBtn);
        
        actionCard.add(actionRow);
        content.add(actionCard);
        content.add(Box.createVerticalStrut(12));

        // 5. 통계 그래프 섹션 (Dashboard에서 전달받음)
        if (graphSectionPanel != null) {
            graphSectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(graphSectionPanel);
        }

        add(content, BorderLayout.NORTH);
    }

    /**
     * 근무지 리스트 데이터를 갱신합니다.
     */
    public void updateCategoryData(List<String> newData) {
        updateCategoryData(newData, null);
    }

    public void updateCategoryData(List<String> newData, String selectedValue) {
        updatingCategoryData = true;
        try {
            String previousSelection = selectedValue != null ? selectedValue : categoryList.getSelectedValue();
            categoryModel.clear();
            if (newData != null) {
                for (String item : newData) {
                    categoryModel.addElement(item);
                }
            }

            if (categoryModel.isEmpty()) {
                categoryList.clearSelection();
            } else if (previousSelection != null && categoryModel.contains(previousSelection)) {
                categoryList.setSelectedValue(previousSelection, true);
            } else {
                categoryList.setSelectedIndex(0);
            }
        } finally {
            updatingCategoryData = false;
        }
    }

    public String getSelectedCategoryValue() {
        return categoryList.getSelectedValue();
    }

    /**
     * 사이드바 내부 카드 공통 스타일 설정
     */
    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(270, Integer.MAX_VALUE));
        return panel;
    }

    /**
     * 관리 버튼 공통 스타일 설정
     */
    private void styleActionButton(JButton button, Color backgroundColor, Color foregroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(backgroundColor.equals(Color.WHITE) ? new Color(209, 213, 219) : backgroundColor, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        
        if (backgroundColor.equals(Color.WHITE)) {
            button.setOpaque(true);
            button.setContentAreaFilled(true);
        }
        
        button.putClientProperty("JButton.buttonType", "roundRect");
        UIManager.put("Button.arc", 14);
    }
}