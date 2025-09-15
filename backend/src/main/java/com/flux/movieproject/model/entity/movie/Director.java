package com.flux.movieproject.model.entity.movie;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Director {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer directorId;
	
	@Column(name = "tmdb_director_id") // 修正點：直接映射為資料庫欄位
	private Integer tmdbDirectorId; // 修正點：直接宣告為 Integer 類型
	
	private String name;
	
	private String directorSummary;
	
	// 在 Director.java 中
	@JsonIgnore
	@OneToMany(mappedBy = "director", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<MovieDirector> movieDirectors = new HashSet<>();

	}
