package com.ticketing.gatewayservice.infrastructure.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * 클레임 정보를 생성하는 헬퍼 클래스입니다.
 */
public class ArbitraryClaimsFactory {

	/**
	 * 캐시된 클레임 정보를 생성하는 헬퍼 메서드입니다.
	 *
	 * @return 생성된 클레임 정보 Map 객체
	 */
	public static Map<String, Object> claims() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("user", "testUser");
		return claims;
	}
}