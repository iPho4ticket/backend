package com.ticketing.gatewayservice.application.filter;

import static com.ticketing.gatewayservice.infrastructure.handler.AuthTokenResponseHandler.*;
import static com.ticketing.gatewayservice.infrastructure.util.CacheUtils.*;
import static com.ticketing.gatewayservice.infrastructure.util.TokenUtils.*;
import static org.springframework.http.HttpHeaders.*;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.ticketing.gatewayservice.infrastructure.cache.CacheService;
import com.ticketing.gatewayservice.infrastructure.feign.AuthWebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * JWT 토큰을 검증하고 필터 체인을 처리하는 필터입니다.
 * 캐시에서 먼저 토큰을 확인하고, 없는 경우 Auth 서버에 요청하여 검증합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter implements GlobalFilter {

	private final AuthWebClient authWebClient;
	private final CacheService cacheService;

	/**
	 * 클라이언트 요청에서 JWT 토큰을 추출하고 이를 검증합니다.
	 * 캐시에 토큰 정보가 있을 경우 캐시된 데이터를 사용하고, 없을 경우 Auth 서버에 토큰 검증을 요청합니다.
	 *
	 * @param exchange 서버 교환 객체 (요청 및 응답 정보를 포함)
	 * @param chain 필터 체인
	 * @return 필터 체인 결과
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String requestPath = exchange.getRequest().getURI().getPath();

		// auth 관련 경로 회피 (로그인, 회원가입 등)
		if (requestPath.startsWith("/api/v1/auth/")) {
			log.info("Auth 관련 요청 - 토큰 검증 회피: {}", requestPath);
			return chain.filter(exchange);  // 필터를 적용하지 않고 바로 다음 체인으로 이동
		}

		String authHeader = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION);
		if (!isValidBearerToken(authHeader))
			return handleUnauthorized(exchange, "Authorization 헤더가 없거나 올바르지 않음.");

		String token = extractToken(authHeader);

		// 캐시에서 먼저 토큰 유효성 확인
		Map<String, Object> cachedClaims = cacheService.getFromCache(token);
		if (isCacheAvailable(cachedClaims)) {
			return processCachedToken(exchange, chain, token, cachedClaims);
		}

		// 캐시 미스 시 WebClient를 통해 토큰 검증 및 캐시에 저장
		return authWebClient.validateToken("Bearer " + token)
			.flatMap(claims -> saveToCacheAndProceed(exchange, chain, claims, token))
			.onErrorResume(e -> handleTokenValidationFailure(exchange, e, token));
	}

	/**
	 * 검증된 토큰 정보를 캐시에 저장한 후 필터 체인을 계속해서 진행합니다.
	 *
	 * @param exchange 서버 교환 객체
	 * @param chain 필터 체인
	 * @param claims 검증된 클레임 정보
	 * @param token 저장할 JWT 토큰
	 * @return 필터 체인 결과
	 */
	private Mono<Void> saveToCacheAndProceed(ServerWebExchange exchange, GatewayFilterChain chain,
		Map<String, Object> claims, String token) {
		cacheService.putInCache(token, claims);
		log.info("토큰 검증 성공: {}, 캐시에 저장", token);
		return handleSuccess(exchange, claims, chain);
	}

	/**
	 * 캐시된 토큰 정보를 사용하여 필터 체인을 계속해서 진행합니다.
	 *
	 * @param exchange 서버 교환 객체
	 * @param chain 필터 체인
	 * @param token 캐시에 있는 JWT 토큰
	 * @param cachedClaims 캐시된 클레임 정보
	 * @return 필터 체인 결과
	 */
	private Mono<Void> processCachedToken(ServerWebExchange exchange, GatewayFilterChain chain, String token,
		Map<String, Object> cachedClaims) {
		log.info("토큰 캐시 히트: {}", token);
		return handleSuccess(exchange, cachedClaims, chain);
	}
}