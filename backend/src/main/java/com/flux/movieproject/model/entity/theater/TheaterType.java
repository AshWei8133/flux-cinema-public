package com.flux.movieproject.model.entity.theater;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class TheaterType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer theaterTypeId;

	private String theaterTypeName;

	private String description;
	
	@OneToMany(fetch = FetchType.LAZY ,mappedBy = "theaterType")
	private List<Theater> theaters = new ArrayList<>();

}
