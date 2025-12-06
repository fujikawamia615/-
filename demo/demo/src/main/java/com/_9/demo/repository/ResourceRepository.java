package com._9.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com._9.demo.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
	Resource findByFileKeyAndFileType(String fileKey, String fileType);
}
