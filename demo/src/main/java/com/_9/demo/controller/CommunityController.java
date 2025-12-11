package com._9.demo.controller;

import com._9.demo.model.Post;
import com._9.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class CommunityController {

    @Autowired
    private PostRepository postRepository;

    // ----------------------------------------------------
    // 1. 获取帖子列表接口 (GET)
    // ----------------------------------------------------
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        // 使用 Repository 中定义的按时间倒序查询方法
        List<Post> posts = postRepository.findAllByOrderByPostTimeDesc();
        return ResponseEntity.ok(posts);
    }

    // ----------------------------------------------------
    // 2. 发布新帖子接口 (POST)
    // DTO: 我们使用 Map 来接收 author, title, content
    // ----------------------------------------------------
    @PostMapping("/post")
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> postData) {
        String author = postData.get("author");
        String title = postData.get("title");
        String content = postData.get("content");

        if (author == null || author.isEmpty()) {
            return ResponseEntity.badRequest().body("发帖人信息缺失。");
        }
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("帖子内容不能为空。");
        }
        
        // 帖子标题可选，但不能太长
        if (title != null && title.length() > 100) {
            title = title.substring(0, 100) + "...";
        }
        
        Post newPost = new Post();
        newPost.setAuthor(author);
        newPost.setTitle(title != null ? title : "");
        newPost.setContent(content);
        // newPost.postTime 在构造函数中自动设置

        postRepository.save(newPost);
        
        return ResponseEntity.ok("帖子发布成功！");
    }
}