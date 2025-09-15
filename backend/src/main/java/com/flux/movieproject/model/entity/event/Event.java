package com.flux.movieproject.model.entity.event; 

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "eventId")
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer eventId; // 活動的唯一識別碼

	private String title; // 活動標題

	private String content; // 活動內容或詳細說明
	
	@ManyToOne(fetch = FetchType.LAZY) 
	@JoinColumn(name = "event_category_id") 
	@JsonIgnore
	private EventCategory eventCategory;

	@Lob // 標記此欄位用於儲存大型二進位物件 (BLOB)
	private byte[] image;

	private Integer sessionCount; // 活動場次數量

	private LocalDate startDate; // 活動開始日期

	private LocalDate endDate; // 活動結束日期 (若無則為 NULL，表示長期活動)

//	private String status; // 活動狀態 (例如 '正在進行', '已經結束')

	// @Transient 表示這個屬性不會被持久化到資料庫
	@Transient
	private String base64ImageString;

}