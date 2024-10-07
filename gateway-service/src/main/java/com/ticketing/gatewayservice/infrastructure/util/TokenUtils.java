package com.ticketing.gatewayservice.infrastructure.util;

/**
 * JWT 토큰 관련 유틸리티 클래스입니다.
 * Bearer 토큰의 유효성 검증 및 토큰을 추출하는 기능을 제공합니다.
 */
public class TokenUtils {

	/**
	 * 주어진 Authorization 헤더가 유효한 Bearer 토큰인지 확인합니다.
	 *
	 * @param authHeader Authorization 헤더 값
	 * @return 유효한 Bearer 토큰일 경우 true, 그렇지 않으면 false
	 */
	public static boolean isValidBearerToken(String authHeader) {
		return authHeader != null && authHeader.startsWith("Bearer ");
	}

	/**
	 * Authorization 헤더에서 Bearer 토큰을 추출합니다.
	 *
	 * @param authHeader Authorization 헤더 값
	 * @return Bearer 토큰 문자열
	 */
	public static String extractToken(String authHeader) {
		return authHeader.substring(7);
	}
}