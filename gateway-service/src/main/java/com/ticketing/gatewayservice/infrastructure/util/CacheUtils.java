package com.ticketing.gatewayservice.infrastructure.util;

import java.util.Map;

/**
 * 캐시 관련 유틸리티 클래스입니다.
 * 캐시된 클레임 정보의 유효성을 확인하는 기능을 제공합니다.
 */
public class CacheUtils {

	/**
	 * 캐시된 클레임 정보가 유효한지 확인합니다.
	 *
	 * @param cachedClaims 캐시에서 조회된 클레임 정보
	 * @return 캐시된 정보가 있을 경우 true, 그렇지 않으면 false
	 */
	public static boolean isCacheAvailable(Map<String, Object> cachedClaims) {
		return cachedClaims != null;
	}
}