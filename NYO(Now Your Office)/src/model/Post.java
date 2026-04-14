package model;

import java.io.Serializable;

// 게시판 글 1개 표현
public class Post implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String type; // 게시글 종류
	private final String title; // 제목
	private final String author; // 작성자
	private final String date; // 작성일
	private final String content; // 본문
	
	public Post(String type, String title, String author, String date, String content) {
		this.type = type;
		this.title = title;
		this.author = author;
		this.date = date;
		this.content = content;
	}
	
	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getContent() {
		return content;
	}
}