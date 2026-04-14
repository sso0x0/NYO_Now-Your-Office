package ui;

import model.Post;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.List;

/**
 * 통합 게시판 전용 패널
 * 
 * 역할:
 * - 게시글 목록을 카드 형태로 표시합니다.
 * - 글쓰기 UI를 제공합니다.
 * - 메인 화면으로 돌아가기 버튼을 제공합니다.
 * 
 * 주의:
 * - 실제 저장(saveDataToFile)은 여기서 하지 않습니다.
 * - 실제 화면 전환(cardLayout)도 여기서 하지 않습니다.
 * - 필요한 동작은 콜백으로 외부(WorkerCalendarAppFinal)에 위임합니다.
 */
public class BoardPanel extends JPanel {

   /**
    * 새 게시글 작성 완료 시 외부로 전달할 콜백
    */
   public interface OnWritePostListener {
      void onWritePost(Post post);
   }

   /**
    * 메인 화면으로 돌아가기 요청 콜백
    */
   public interface OnBackListener {
      void onBack();
   }

   // 색상 테마
   private final Color colorBackgroundGray;
   private final Color colorTextDark;
   private final Color colorTextGray;
   private final Color colorTossBlue;
   private final Color colorBorderLine;

   // 현재 작성자 이름
   private final String authorName;

   // 게시글 목록
   private final List<Post> boardDatabase;

   // 외부 위임 콜백
   private final OnWritePostListener onWritePostListener;
   private final OnBackListener onBackListener;

   // 게시글 카드들이 들어갈 영역
   private JPanel feedContainer;

   /**
    * 생성자
    * 
    * @param colorBackgroundGray 배경 회색
    * @param colorTextDark       진한 글자색
    * @param colorTextGray       보조 글자색
    * @param colorTossBlue       강조 파란색
    * @param colorBorderLine     테두리색
    * @param authorName          현재 작성자 이름
    * @param boardDatabase       게시글 목록
    * @param onWritePostListener 글쓰기 완료 콜백
    * @param onBackListener      뒤로가기 콜백
    */
   public BoardPanel(
         Color colorBackgroundGray,
         Color colorTextDark,
         Color colorTextGray,
         Color colorTossBlue,
         Color colorBorderLine,
         String authorName,
         List<Post> boardDatabase,
         OnWritePostListener onWritePostListener,
         OnBackListener onBackListener) {

      this.colorBackgroundGray = colorBackgroundGray;
      this.colorTextDark = colorTextDark;
      this.colorTextGray = colorTextGray;
      this.colorTossBlue = colorTossBlue;
      this.colorBorderLine = colorBorderLine;
      this.authorName = authorName;
      this.boardDatabase = boardDatabase;
      this.onWritePostListener = onWritePostListener;
      this.onBackListener = onBackListener;

      initializeUI();
      refreshFeed();
   }

   /**
    * 게시판 전체 UI를 구성합니다.
    */
   private void initializeUI() {
      setLayout(new BorderLayout(0, 15));
      setBackground(colorBackgroundGray);
      setBorder(new EmptyBorder(20, 20, 20, 20));

      // 상단 헤더
      JPanel topHeaderBox = new JPanel(new BorderLayout());
      topHeaderBox.setBackground(colorBackgroundGray);

      JLabel boardTitle = new JLabel("📢 사내 통합 커뮤니케이션 피드");
      boardTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));

      JButton writeArticleBtn = new JButton("✏️ 글쓰기");
      writeArticleBtn.setPreferredSize(new Dimension(100, 35));
      writeArticleBtn.setBackground(colorTossBlue);
      writeArticleBtn.setForeground(Color.WHITE);
      writeArticleBtn.setFocusPainted(false);

      // 글쓰기 버튼 클릭 시 게시글 작성 다이얼로그 열기
      writeArticleBtn.addActionListener(e -> openWriteDialog());

      topHeaderBox.add(boardTitle, BorderLayout.WEST);
      topHeaderBox.add(writeArticleBtn, BorderLayout.EAST);

      // 피드 영역
      feedContainer = new JPanel();
      feedContainer.setLayout(new BoxLayout(feedContainer, BoxLayout.Y_AXIS));
      feedContainer.setBackground(colorBackgroundGray);
      feedContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

      JScrollPane scrollArea = new JScrollPane(feedContainer);
      scrollArea.setBorder(null);
      scrollArea.getVerticalScrollBar().setUnitIncrement(20);

      // 하단 돌아가기 버튼
      JButton goBackBtn = new JButton("◀ 메인으로 돌아가기");
      goBackBtn.setPreferredSize(new Dimension(150, 45));
      goBackBtn.setFocusPainted(false);
      goBackBtn.setBackground(Color.WHITE);
      goBackBtn.setForeground(colorTextDark);
      goBackBtn.setBorder(BorderFactory.createLineBorder(colorBorderLine, 1, true));

      goBackBtn.addActionListener(e -> {
         if (onBackListener != null) {
            onBackListener.onBack();
         }
      });

      JPanel bottomWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
      bottomWrap.setBackground(colorBackgroundGray);
      bottomWrap.add(goBackBtn);

      add(topHeaderBox, BorderLayout.NORTH);
      add(scrollArea, BorderLayout.CENTER);
      add(bottomWrap, BorderLayout.SOUTH);
   }

   /**
    * 현재 게시글 목록을 기준으로 피드를 다시 그립니다.
    * 
    * 역할:
    * - 기존 카드 제거
    * - 최신 boardDatabase 기준으로 카드 다시 생성
    */
   public void refreshFeed() {
      feedContainer.removeAll();

      if (boardDatabase != null) {
         for (Post post : boardDatabase) {
            feedContainer.add(buildSinglePostCard(post));
            feedContainer.add(Box.createVerticalStrut(15));
         }
      }

      feedContainer.revalidate();
      feedContainer.repaint();
   }

   /**
    * 글쓰기 입력창을 열고, 입력 완료 시 외부 콜백으로 새 Post를 전달합니다.
    */
   private void openWriteDialog() {
      JTextField titleInput = new JTextField();
      JTextArea contentInput = new JTextArea(5, 20);

      Object[] popUpElements = {
            "제목을 입력하세요:",
            titleInput,
            "내용을 작성해주세요:",
            new JScrollPane(contentInput)
      };

      int result = JOptionPane.showConfirmDialog(
            this,
            popUpElements,
            "새 게시글 작성",
            JOptionPane.OK_CANCEL_OPTION
      );

      // 취소 시 종료
      if (result != JOptionPane.OK_OPTION) {
         return;
      }

      // 제목이 비어 있으면 저장하지 않음
      if (titleInput.getText().trim().isEmpty()) {
         JOptionPane.showMessageDialog(this, "제목을 입력해주세요.");
         return;
      }

      // 새 Post 생성
      Post newPost = new Post(
            "일반",
            titleInput.getText().trim(),
            authorName,
            LocalDate.now().toString(),
            contentInput.getText()
      );

      // 실제 저장은 외부에 위임
      if (onWritePostListener != null) {
         onWritePostListener.onWritePost(newPost);
      }
   }

   /**
    * 게시글 1개를 카드 형태 UI로 생성합니다.
    * 
    * @param post 게시글 데이터
    * @return 게시글 카드 패널
    */
   private JPanel buildSinglePostCard(Post post) {
      JPanel cardBg = new JPanel(new BorderLayout(10, 10));
      cardBg.setBackground(Color.WHITE);
      cardBg.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
            new EmptyBorder(20, 20, 20, 20)
      ));
      cardBg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

      JPanel profileHeader = new JPanel(new BorderLayout());
      profileHeader.setOpaque(false);

      JLabel authorLbl = new JLabel("👤 " + post.getAuthor());
      authorLbl.setFont(new Font("맑은 고딕", Font.BOLD, 13));
      authorLbl.setForeground(colorTextDark);

      JLabel dateLbl = new JLabel(post.getDate());
      dateLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
      dateLbl.setForeground(colorTextGray);

      profileHeader.add(authorLbl, BorderLayout.WEST);
      profileHeader.add(dateLbl, BorderLayout.EAST);

      JLabel titleLbl = new JLabel(post.getTitle());
      titleLbl.setFont(new Font("맑은 고딕", Font.BOLD, 17));
      titleLbl.setForeground(colorTextDark);

      // 줄바꿈을 HTML <br>로 변환하여 카드 안에서 자연스럽게 표시
      String parsedContent = post.getContent().replace("\n", "<br>");
      JLabel contentLbl = new JLabel(
            "<html><p style='width:600px; color:#6B7684;'>" + parsedContent + "</p></html>"
      );
      contentLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

      JPanel contentCenter = new JPanel(new BorderLayout(0, 8));
      contentCenter.setOpaque(false);
      contentCenter.add(titleLbl, BorderLayout.NORTH);
      contentCenter.add(contentLbl, BorderLayout.CENTER);

      cardBg.add(profileHeader, BorderLayout.NORTH);
      cardBg.add(contentCenter, BorderLayout.CENTER);

      return cardBg;
   }
}