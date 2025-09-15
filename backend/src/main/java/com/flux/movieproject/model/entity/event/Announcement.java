package com.flux.movieproject.model.entity.event;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
public class Announcement {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer announcementId; 

	private String title;

	private String content;

	@Lob
	private byte[] announcementImage;

	private LocalDate publishDate;

	@Transient 
    private String base64ImageString;

}
