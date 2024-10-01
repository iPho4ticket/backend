package com.ticketing.userservice.application.dto;

import java.time.LocalDateTime;

import com.ticketing.userservice.infrastructure.common.RoleType;

import lombok.Builder;
import lombok.With;

public interface UserDto {

	interface Delete {
		@With
		@Builder
		record Soft(Long id, Long deleterId

		) {

		}

		@With
		@Builder
		record Result(Long id, Long deleterId, LocalDateTime deletedAt

		) {

		}
	}

	@With
	@Builder
	record Create(String name, String email, String password, String phoneNumber, RoleType role) {

	}

	@With
	@Builder
	record Result(Long id, String name, String email, String password, String phoneNumber, RoleType role

	) {

	}
}
