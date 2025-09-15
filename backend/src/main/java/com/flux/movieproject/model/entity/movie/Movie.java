package com.flux.movieproject.model.entity.movie;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Movie {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "movie_id")
	private Integer id; 

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY) 
	@JoinColumn(name = "tmdb_movie_id",foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)) 
	private TMDBMovie tmdbMovie; 

	private String titleLocal; 

	private String titleEnglish; 

	private LocalDate releaseDate; 

	private LocalDate offShelfDate; 

	private String certification; 

	private String overview; 

	private String trailerUrl; 

	private Integer durationMinutes; 

	private byte[] posterImage; 

	private String originalLanguage; 

	private Double popularity; 

	private Double voteAverage; 

	private Integer voteCount; 

	private Boolean status; // 是否上架 

	@Lob
	@Column(name = "preview_image_1")
	private byte[] previewImage1;

	@Lob
	@Column(name = "preview_image_2")
	private byte[] previewImage2;

	@Lob
	@Column(name = "preview_image_3")
	private byte[] previewImage3;

	@Lob
	@Column(name = "preview_image_4")
	private byte[] previewImage4;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//前端對應的時間格式，要搭配雙層大括號 
	@Temporal(TemporalType.TIMESTAMP) 
	private LocalDateTime createTime; 

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//前端對應的時間格式，要搭配雙層大括號 
	@Temporal(TemporalType.TIMESTAMP) 
	private LocalDateTime updateTime; 

	// --- 關聯 (改為 OneToMany) ---
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<MovieDirector> movieDirectors = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<MovieGenre> movieGenres = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<MovieActor> movieActors = new HashSet<>();
    
    @Transient
    private List<Integer> selectedGenreIds; 
}
