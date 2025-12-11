package com._9.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发帖人 (与 User 实体关联，但此处简化为存储用户名)
    @Column(nullable = false)
    private String author;

    // 帖子标题（可选，但推荐）
    @Column(length = 100)
    private String title;
    
    // 帖子内容
    @Column(nullable = false, length = 2000) // 限制内容长度
    private String content;

    // 发布时间
    @Column(nullable = false)
    private LocalDateTime postTime;
    
    // 默认点赞数 (可选)
    private Integer likes = 0;

    // 构造函数
    public Post() {
        this.postTime = LocalDateTime.now(); // 默认设置为当前时间
    }

    // --- Getters and Setters ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getPostTime() { return postTime; }
    // 注意：通常不提供 setPostTime，让构造函数自动设置

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
}