package com._9.demo.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import com._9.demo.repository.ResourceRepository;
import com._9.demo.repository.UserRepository;
import com._9.demo.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ResourceController {

	private final ResourceRepository resourceRepository;
	private final UserRepository userRepository;
	private static final String FILE_ROOT = "/root/";

	public ResourceController(ResourceRepository resourceRepository, UserRepository userRepository) {
		this.resourceRepository = resourceRepository;
		this.userRepository = userRepository;
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名已存在!");
		}
		User newUser = new User();
		newUser.setUsername(user.getUsername());
		newUser.setPassword(user.getPassword());
		newUser.setDownloadHistory("{}");
		userRepository.save(newUser);
		return ResponseEntity.ok("注册成功!");
	}

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
		return userRepository.findByUsername(request.getUsername()).map(user -> {
			if (!user.getPassword().equals(request.getOldPassword())) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("旧密码错误!");
			}
			if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
				return ResponseEntity.badRequest().body("新密码至少需要 6 位!");
			}
			user.setPassword(request.getNewPassword());
			userRepository.save(user);

			return ResponseEntity.ok("密码更改成功，请重新登录。");
		}).orElseGet(() -> {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户不存在或旧密码错误!");
		});
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
		return userRepository.findByUsername(loginRequest.getUsername()).map(user -> {
			if (user.getPassword().equals(loginRequest.getPassword())) {
				return ResponseEntity.ok("登录成功");
			} else {
				return ResponseEntity.badRequest().body("用户名或密码错误");
			}
		}).orElseGet(() -> {
			return ResponseEntity.badRequest().body("用户名或密码错误");
		});
	}

	@GetMapping("/resources")
	public List<com._9.demo.model.Resource> getAllResources() {
		return resourceRepository.findAll();
	}

	@GetMapping("/stream/resource/{type}/{fileKey}")
	public ResponseEntity<org.springframework.core.io.Resource> streamResource(@PathVariable String type,
			@PathVariable String fileKey, HttpServletRequest request) {
		com._9.demo.model.Resource resource = resourceRepository.findByFileKeyAndFileType(fileKey, type);

		if (resource == null) {
			return ResponseEntity.notFound().build();
		}

		String fullPath = FILE_ROOT + type + "/" + fileKey;
		File file = new File(fullPath);

		if (!file.exists() || !file.canRead()) {
			return ResponseEntity.notFound().build();
		}

		String contentType = getContentType(fileKey);

		try {
			return serveStream(file, contentType, request);
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/download/resource/{type}/{fileKey}")
	public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable String type,
			@PathVariable String fileKey, @RequestParam(required = true) String username) {
		com._9.demo.model.Resource resource = resourceRepository.findByFileKeyAndFileType(fileKey, type);

		if (resource == null) {
			return ResponseEntity.notFound().build();
		}
		resource.setTimes(resource.getTimes() + 1);
		resourceRepository.save(resource);

		userRepository.findByUsername(username).ifPresent(user -> {
			updateDownloadHistory(user, resource.getName());
		});

		return serveFile(type, fileKey, false);
	}

	@PostMapping("/download-history")
	public ResponseEntity<?> getDownloadHistory(@RequestBody java.util.Map<String, String> requestBody) {

		String username = requestBody.get("username");

		if (username == null || username.isEmpty()) {
			return ResponseEntity.badRequest().body("缺少用户名参数。");
		}

		java.util.Optional<com._9.demo.model.User> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			com._9.demo.model.User user = userOptional.get();
			return ResponseEntity.ok(user.getDownloadHistory());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户不存在。");
		}
	}

	@GetMapping("/download/cover/{coverName}")
	public ResponseEntity<org.springframework.core.io.Resource> downloadCover(@PathVariable String coverName) {
		return serveFile("封面", coverName, true);
	}

	private String getContentType(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return "application/octet-stream";
		}

		String lowerCaseFileName = fileName.toLowerCase();

		if (lowerCaseFileName.endsWith(".mp4") || lowerCaseFileName.endsWith(".m4v")) {
			return "video/mp4";
		}
		if (lowerCaseFileName.endsWith(".flac")) {
			return "audio/flac";
		}
		if (lowerCaseFileName.endsWith(".mp3")) {
			return "audio/mpeg";
		}
		if (lowerCaseFileName.endsWith(".ogg") || lowerCaseFileName.endsWith(".oga")) {
			return "audio/ogg";
		}
		if (lowerCaseFileName.endsWith(".txt")) {
			return "text/plain; charset=GBK";
		}

		return "application/octet-stream";
	}

	private ResponseEntity<org.springframework.core.io.Resource> serveFile(String type, String fileName,
			boolean isInline) {

		String fullPath = FILE_ROOT + type + "/" + fileName;
		File file = new File(fullPath);

		try {
			org.springframework.core.io.Resource resource = new UrlResource(file.toURI());

			if (!resource.exists() || !resource.isReadable()) {
				return ResponseEntity.notFound().build();
			}

			String encodedFilename = java.net.URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentLength(file.length());

			if (isInline) {
				headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFilename + "\"");
				if (type.equals("封面"))
					headers.set(HttpHeaders.CONTENT_TYPE, "image/jpeg");
			} else {
				headers.set(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
				headers.set(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
			}

			return ResponseEntity.ok().headers(headers).body(resource);

		} catch (MalformedURLException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	private ResponseEntity<org.springframework.core.io.Resource> serveStream(File file, String contentType,
			HttpServletRequest request) throws IOException {

		org.springframework.core.io.Resource resource = new UrlResource(file.toURI());
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
		headers.set(HttpHeaders.CONTENT_TYPE, contentType);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"");
		String rangeHeader = request.getHeader(HttpHeaders.RANGE);

		if (rangeHeader != null) {
			return ResponseEntity.status(HttpStatus.OK).headers(headers).contentLength(file.length()).body(resource);

		} else {
			return ResponseEntity.status(HttpStatus.OK).headers(headers).contentLength(file.length()).body(resource);
		}
	}

	private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson JSON 处理实例

	private void updateDownloadHistory(User user, String resourceTitle) {
		try {
			TypeReference<java.util.Map<String, Integer>> typeRef = new TypeReference<java.util.Map<String, Integer>>() {
			};
			java.util.Map<String, Integer> historyMap = objectMapper.readValue(user.getDownloadHistory(), typeRef);
			historyMap.merge(resourceTitle, 1, Integer::sum);
			user.setDownloadHistory(objectMapper.writeValueAsString(historyMap));
			userRepository.save(user);

		} catch (IOException e) {
			System.err
					.println("Error updating download history for user " + user.getUsername() + ": " + e.getMessage());
		}
	}
}