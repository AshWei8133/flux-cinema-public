package com.flux.movieproject.model.entity.movie;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Embeddable
public class MovieActorId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "movie_id")
    private Integer movieId;
    @Column(name = "actor_id")
    private Integer actorId;
}
