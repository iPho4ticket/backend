package com.ticketing.gatewayservice.infrastructure.cache;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * Caffeine 기반의 캐시 관리 서비스입니다.
 * 외부 환경 변수에서 캐시 만료 시간과 최대 크기를 주입받아 설정합니다.
 */
@Component
@RequiredArgsConstructor
public class CaffeineCacheService implements CacheService {

	private Cache<String, Map<String, Object>> localCache;
	@Value("${cache.caffeine.expiration}")
	private long expirationTime;  // 캐시 만료 시간
	@Value("${cache.caffeine.max-size}")
	private long maxSize;  // 캐시 최대 크기

	/**
	 * CaffeineCacheService의 생성자입니다.
	 * 캐시 만료 시간과 크기를 외부 설정으로 받아 초기화합니다.
	 */
	@PostConstruct
	public void initCache() {

		this.localCache = Caffeine.newBuilder()
			.expireAfterWrite(expirationTime, TimeUnit.SECONDS)  // 캐시 만료 시간 설정
			.maximumSize(maxSize)  // 캐시 최대 크기 설정
			.build();
	}

	/**
	 * 캐시에서 토큰에 해당하는 클레임 정보를 조회합니다.
	 *
	 * @param token 검증할 JWT 토큰
	 * @return 캐시된 클레임 정보 또는 null
	 */
	@Override
	public Map<String, Object> getFromCache(String token) {
		return localCache.getIfPresent(token);  // 캐시에서 토큰 정보 조회
	}

	/**
	 * 토큰과 해당 클레임 정보를 캐시에 저장합니다.
	 *
	 * @param token 캐시에 저장할 토큰
	 * @param claims 토큰에 해당하는 클레임 정보
	 */
	@Override
	public void putInCache(String token, Map<String, Object> claims) {
		if (claims != null && !claims.isEmpty()) {
			localCache.put(token, claims);  // 캐시에 저장
		}
	}
}