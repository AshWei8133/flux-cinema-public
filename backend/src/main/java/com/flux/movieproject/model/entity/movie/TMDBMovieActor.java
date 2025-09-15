package com.flux.movieproject.model.entity.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tmdb_movie_actor")
public class TMDBMovieActor {
    @EmbeddedId
    private TMDBMovieActorId id = new TMDBMovieActorId();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tmdbMovieId")
    @JoinColumn(name = "tmdb_movie_id")
    private TMDBMovie tmdbMovie;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tmdbActorId")
    @JoinColumn(name = "tmdb_actor_id")
    private TMDBActor tmdbActor;

    @Column(name = "character", length = 100)
    private String character;

    @Column(name = "order_num")
    private Integer orderNum;
}
