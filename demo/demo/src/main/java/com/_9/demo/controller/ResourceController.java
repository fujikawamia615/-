package com._9.demo.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import com._9.demo.repository.ResourceRepository;
import com._9.demo.repository.UserRepository;
import com._9.demo.model.User;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ResourceController(ResourceRepository resourceRepository,UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }
    @PostMapping("/register") // 使用 POST 请求处理注册
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // 1. 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            // 返回 409 Conflict 或 400 Bad Request
            return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名已存在!"); 
        }

        // 2. ⚠️ 实际项目中：对密码进行加密，例如使用 BCrypt

        // 3. 设置默认值并保存用户
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword());
        newUser.setDownloadHistory("{}"); // 初始化下载记录
        
        userRepository.save(newUser);

        // 返回成功信息
        return ResponseEntity.ok("注册成功!");
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        // 1. 根据用户名查找用户
        return userRepository.findByUsername(loginRequest.getUsername())
            .map(user -> {
                // 2. 找到用户后，直接比较明文密码
                if (user.getPassword().equals(loginRequest.getPassword())) {
                    // 登录成功
                    // 实际项目中这里会生成 Session 或 Token
                    // 练习项目中我们只返回一个成功信号
                    return ResponseEntity.ok("登录成功");
                } else {
                    // 密码错误
                    return ResponseEntity.badRequest().body("用户名或密码错误");
                }
            })
            .orElseGet(() -> {
                // 用户名不存在
                return ResponseEntity.badRequest().body("用户名或密码错误");
            });
    }
    @GetMapping("/resources")
    public List<com._9.demo.model.Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    @GetMapping("/download/resource/{type}/{fileKey}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(
            @PathVariable String type,
            @PathVariable String fileKey) {

        // 1. 先根据 fileKey 和 type 找到资源（假设 fileKey 是唯一标识）
        com._9.demo.model.Resource resource = resourceRepository.findByFileKeyAndFileType(fileKey, type);
        
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 递增点击次数
        resource.setTimes(resource.getTimes() + 1);
        resourceRepository.save(resource); // 保存回数据库

        // 3. 正常返回文件
        return serveFile(type, fileKey, false);
    }

    @GetMapping("/download/cover/{coverName}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadCover(
            @PathVariable String coverName) {
        return serveFile("封面", coverName, true);
    }

    private ResponseEntity<org.springframework.core.io.Resource> serveFile(
            String type, String fileName, boolean isInline) {


        String fullPath = "/root/" + type + "/" + fileName;
        File file = new File(fullPath);

        try {
            org.springframework.core.io.Resource resource = new UrlResource(file.toURI());


            String encodedFilename = java.net.URLEncoder.encode(file.getName(), "UTF-8")
                    .replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentLength(file.length());

  
            if (isInline) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFilename + "\"");
            } else {
                headers.set(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


}