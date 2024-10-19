package com.ticketing.authzfilter.security;

import static com.ticketing.authzfilter.infrastructure.common.CustomException.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ticketing.authzfilter.infrastructure.common.codes.ErrorCode;

/**
 * SecurityUtil
 * <p>
 * 현재 인증된 사용자의 정보를 가져오는 유틸리티 클래스입니다.
 * SecurityContextHolder로부터 현재 사용자의 인증 정보를 추출하여 필요한 정보를 반환합니다.
 */
public class SecurityUtil {

	/**
	 * 현재 인증된 사용자의 userId를 반환합니다.
	 *
	 * @return Long 현재 인증된 사용자의 ID
	 * @throws AuthenticationNotFoundException 인증 정보가 없을 경우
	 * @throws InvalidIdFormatException userId 형식이 잘못되었을 경우
	 */
	public static Long getUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 인증 정보가 없을 경우 예외 발생
		if (authentication == null) {
			throw new AuthenticationNotFoundException(ErrorCode.INVALID_TOKEN, "인증 정보가 존재하지 않습니다.");
		}

		// 인증된 사용자 정보가 존재할 경우 처리
		if (authentication.getPrincipal() instanceof String userId) {
			try {
				return Long.parseLong(userId);  // userId가 올바른 형식일 경우 반환
			} catch (NumberFormatException e) {
				throw new InvalidIdFormatException(userId);  // 형식이 잘못된 경우 예외 발생
			}
		}

		// principal이 문자열이 아닌 경우 예외 발생
		throw new InvalidIdFormatException("잘못된 인증 정보 형식입니다.");
	}
}