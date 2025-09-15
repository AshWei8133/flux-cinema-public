package com.flux.movieproject.model.entity.movie;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class TMDBMovieGenreId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "tmdb_movie_id")
    private Integer tmdbMovieId;
    @Column(name = "tmdb_genre_id")
    private Integer tmdbGenreId;
}
