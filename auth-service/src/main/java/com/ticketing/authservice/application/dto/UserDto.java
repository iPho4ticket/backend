package com.ticketing.authservice.application.dto;

import com.ticketing.authservice.infrastructure.common.RoleType;

import lombok.Builder;
import lombok.With;

/**
 * UserDto는 사용자 정보와 관련된 모든 요청/응답 데이터를 담는 DTO입니다.
 */
public interface UserDto { //TODO: 공통모듈 적용시 통합

	/**
	 * Auth 관련 요청/응답을 담는 인터페이스입니다.
	 */
	interface Auth {

		/**
		 * 로그인 요청을 처리하는 DTO입니다.
		 *
		 * @param email 사용자 이메일 주소
		 * @param password 사용자 비밀번호
		 */
		@With
		@Builder
		record Login(String email, String password) {
		}

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

	/**
	 * 사용자 정보를 반환하는 DTO입니다.
	 *
	 * @param id 사용자 ID
	 * @param name 사용자 이름
	 * @param email 사용자 이메일 주소
	 * @param role 사용자 권한 정보 (RoleType)
	 */
	@With
	@Builder
	record Result(Long id, String name, String email, RoleType role) {
	}

	/**
	 * 사용자 생성 요청을 처리하는 DTO입니다.
	 *
	 * @param name 사용자 이름
	 * @param email 사용자 이메일 주소
	 * @param password 사용자 비밀번호
	 * @param phoneNumber 사용자 전화번호
	 * @param role 사용자 권한 정보
	 */
	@With
	@Builder
	record Create(String name, String email, String password, String phoneNumber, RoleType role) {
	}
}