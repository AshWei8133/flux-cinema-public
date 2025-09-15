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
public class Actor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer actorId;

	@Column(name = "tmdb_actor_id")
	private Integer tmdbActorId;

	private String name;

	private String biography;

	// --- 反向關聯 ---
	@JsonIgnore
	@OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<MovieActor> movieActors = new HashSet<>();
}
