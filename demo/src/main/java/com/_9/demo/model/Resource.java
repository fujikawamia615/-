package com._9.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resource")
@Data
@NoArgsConstructor
public class Resource {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private String cover;
	
	@Column(name = "file_key")
	private String fileKey;
	private int times = 0;
	private Long size;

	@Column(name = "file_type")
	private String fileType;

	@Column(name = "upload_time")
	private java.sql.Timestamp uploadTime;
}
