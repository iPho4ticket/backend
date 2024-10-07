package com.ticketing.authservice.infrastructure.common;

import java.util.UUID;

import com.ticketing.authservice.infrastructure.common.codes.ErrorCode;

import lombok.Getter;

/**
 * [공통 예외 클래스] API에서 발생하는 모든 예외를 관리하는 클래스.
 * 이 클래스는 비즈니스 로직 내에서 발생하는 모든 예외를 처리하며,
 * 각 예외는 {@link ErrorCode}를 통해 구분된다.
 *
 * <p>이 클래스는 기본적으로 {@link RuntimeException}을 확장하여
 * 런타임 예외로 처리되며, 추가적으로 커스텀 메시지를 포함할 수 있다.
 */
@Getter
public class CustomException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String customMessage;
	private final String errorId;

	/**
	 * 기본 생성자. 에러 ID는 접두사를 붙여 쉽게 추적할 수 있게 구성.
	 *
	 * @param errorCode 에러 코드 (ErrorCode)
	 */
	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
		this.customMessage = null;
		this.errorId = generateErrorId(); // 에러 ID 생성
	}

	/**
	 * 커스텀 메시지와 함께 에러를 생성하는 생성자.
	 *
	 * @param errorCode     에러 코드 (ErrorCode)
	 * @param customMessage 커스텀 메시지 (String)
	 */
	public CustomException(ErrorCode errorCode, String customMessage) {
		super(customMessage != null ? customMessage : errorCode.getMessage());
		this.errorCode = errorCode;
		this.customMessage = customMessage;
		this.errorId = generateErrorId(); // 에러 ID 생성
	}

	/**
	 * 에러 ID 생성 메서드. 접두사와 함께 짧은 UUID를 생성.
	 *
	 * @return 접두사가 포함된 에러 ID
	 */
	private String generateErrorId() {
		return "ERR-" + UUID.randomUUID().toString().substring(0, 8); // UUID 앞 8자리만 사용
	}

	/**
	 * 비밀번호 불일치 예외를 처리하는 내부 클래스.
	 */
	@Getter
	public static class PasswordMismatchException extends CustomException {

		/**
		 * 비밀번호 불일치 시 발생하는 예외.
		 *
		 * @param userId    사용자 ID
		 */
		public PasswordMismatchException(Long userId) {
			super(ErrorCode.PASSWORD_MISMATCH,
				String.format("Password mismatch for User ID (%d)", userId));
		}

		/**
		 * 비밀번호 불일치 시 발생하는 예외 (UUID 기반).
		 *
		 * @param userId    사용자 UUID
		 */
		public PasswordMismatchException(UUID userId) {
			super(ErrorCode.PASSWORD_MISMATCH,
				String.format("Password mismatch for User UUID (%s)", userId.toString()));
		}
	}
}