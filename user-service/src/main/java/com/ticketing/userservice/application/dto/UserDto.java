package com.ticketing.userservice.application.dto;

import java.time.LocalDateTime;

import com.ticketing.userservice.infrastructure.common.RoleType;

import lombok.Builder;
import lombok.With;

public interface UserDto { //TODO: 공통모듈 적용시 통합

	/**
	 * Auth 관련 요청/응답을 담는 인터페이스입니다.
	 */
	interface Auth {

		/**
		 * 로그인 결과를 반환하는 DTO입니다. (readUserByEmail의 결과로 사용)
		 *
		 * @param id 사용자 ID
		 * @param name 사용자 이름
		 * @param email 사용자 이메일 주소
		 * @param password 사용자 비밀번호 (해시된 비밀번호)
		 * @param role 사용자 권한 정보
		 */
		@With
		@Builder
		record Result(Long id, String name, String email, String password, RoleType role) {
		}
	}

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
