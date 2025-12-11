package com._9.demo.repository;

import com._9.demo.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 按发布时间降序查找所有帖子（最新的在前面）
    List<Post> findAllByOrderByPostTimeDesc();
}