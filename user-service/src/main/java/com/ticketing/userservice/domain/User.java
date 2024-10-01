package com.ticketing.userservice.domain;

import com.ticketing.userservice.domain.auditing.BaseEntity;
import com.ticketing.userservice.infrastructure.common.RoleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	private Long id;

	@Setter
	@Column(nullable = false)
	private String name;

	private String phoneNumber;

	@Setter
	@Column(nullable = false)
	private String password;

	@Setter
	private String email;

	@Setter
	@Enumerated(EnumType.STRING)
	private RoleType role;

	@Builder
	public User(String name, String phoneNumber, String password, String email, RoleType role) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.email = email;
		this.role = role;
	}
}
