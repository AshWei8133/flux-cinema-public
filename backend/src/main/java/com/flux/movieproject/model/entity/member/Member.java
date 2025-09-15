package com.flux.movieproject.model.entity.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "member")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer memberId;

	@Column(unique = true)
	private String username;

	private String password;

	private String gender;

	private LocalDate birthDate;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String phone;

	@Lob
	private byte[] profilePhoto;

	@Column(updatable = false)
	private LocalDateTime registerDate;

	private String thirdPartyLoginId;

	private String loginMethod;

	private LocalDateTime lastLoginTime;

	private Integer memberPoints;

}
