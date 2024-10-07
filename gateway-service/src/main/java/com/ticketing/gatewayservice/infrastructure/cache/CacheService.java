package com.ticketing.gatewayservice.infrastructure.cache;

import java.util.Map;

/**
 * 캐시 관리 인터페이스.
 * 다양한 캐시 구현체(Caffeine, Redis 등)를 지원하기 위한 추상화 레이어.
 */
public interface CacheService {

	Map<String, Object> getFromCache(String token);

	void putInCache(String token, Map<String, Object> claims);
}