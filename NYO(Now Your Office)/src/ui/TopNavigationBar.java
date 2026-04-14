package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 상단 메뉴 바 전용 패널입니다.
 *
 * 현재 메인 대시보드에서는 빠른 액션 버튼 줄을 주로 사용하지만,
 * 이 클래스는 상단 메뉴 구조를 다시 쓸 때 바로 가져다 쓸 수 있도록 유지합니다.
 *
 * 초보 팀원 수정 포인트:
 * - 메뉴 버튼 종류 추가/삭제
 * - 사용자 이름 표시 형식 변경
 * - 상단 메뉴 여백/폰트 조정
 */
public class TopNavigationBar extends JPanel {

    public interface OnMenuClickListener {
        void onDashboard();
        void onMyPage();
        void onBoard();
        void onLogout();
    }

    private final OnMenuClickListener listener;
    private final Color colorBackground;
    private final Color colorText;

    private JLabel userLabel;

    public TopNavigationBar(Color colorBackground, Color colorText, OnMenuClickListener listener) {
        this.colorBackground = colorBackground;
        this.colorText = colorText;
        this.listener = listener;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(colorBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel leftMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftMenu.setOpaque(false);

        JButton dashboardBtn = createMenuButton("대시보드");
        JButton myPageBtn = createMenuButton("마이페이지");
        JButton boardBtn = createMenuButton("게시판");

        dashboardBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onDashboard();
            }
        });
        myPageBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onMyPage();
            }
        });
        boardBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onBoard();
            }
        });

        leftMenu.add(dashboardBtn);
        leftMenu.add(myPageBtn);
        leftMenu.add(boardBtn);

        JPanel rightMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightMenu.setOpaque(false);

        userLabel = new JLabel("사용자");
        userLabel.setForeground(colorText);

        JButton logoutBtn = createMenuButton("로그아웃");
        logoutBtn.addActionListener(e -> {
            if (listener != null) {
                listener.onLogout();
            }
        });

        rightMenu.add(userLabel);
        rightMenu.add(logoutBtn);

        add(leftMenu, BorderLayout.WEST);
        add(rightMenu, BorderLayout.EAST);
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(colorText);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        return btn;
    }

    public void setUserName(String name) {
        userLabel.setText("사용자: " + name);
    }
}
