package com._9.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com._9.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查找用户，用于登录和检查用户名是否已存在
    Optional<User> findByUsername(String username);
}