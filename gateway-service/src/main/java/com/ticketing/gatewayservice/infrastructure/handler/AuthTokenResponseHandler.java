package com.ticketing.gatewayservice.infrastructure.handler;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * JWT 토큰 인증 필터에서의 응답을 처리하는 핸들러 클래스입니다.
 * 성공 및 실패 응답을 처리합니다.
 */
@Slf4j
public class AuthTokenResponseHandler {

	/**
	 * 요청이 성공적으로 처리된 경우 200 OK 상태 코드를 설정하고 필터 체인을 계속해서 진행합니다.
	 *
	 * @param exchange 서버 교환 객체
	 * @param chain 필터 체인
	 * @param claims 검증된 클레임 정보
	 * @return 필터 체인 결과
	 */
	public static Mono<Void> handleSuccess(ServerWebExchange exchange, Map<String, Object> claims,
		GatewayFilterChain chain) {
		// 헤더에 유저 정보 추가
		addHeadersToRequest(exchange, claims);
		exchange.getResponse().setStatusCode(HttpStatus.OK);  // 응답 상태 200 설정
		return chain.filter(exchange);
	}

	/**
	 * 인증 실패 시 401 Unauthorized 응답을 반환합니다.
	 *
	 * @param exchange 서버 교환 객체
	 * @param message 실패 원인 메시지
	 * @return 401 Unauthorized 응답
	 */
	public static Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
		log.warn("Unauthorized 접근: {}", message);
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

	/**
	 * 토큰 검증 실패 시 처리하는 메서드입니다.
	 * 검증 실패의 이유를 로깅하고, 401 Unauthorized 상태를 반환합니다.
	 *
	 * @param exchange 서버 교환 객체
	 * @param e 검증 실패의 원인 예외
	 * @param token 검증 실패한 JWT 토큰
	 * @return 401 Unauthorized 응답 처리
	 */
	public static Mono<Void> handleTokenValidationFailure(ServerWebExchange exchange, Throwable e, String token) {
		log.error("토큰 검증 실패: {}, 이유: {}", token, e.getMessage());
		return handleUnauthorized(exchange, "토큰 검증 실패");
	}

	/**
	 * 요청을 금지 처리하고 응답을 완료합니다.
	 *
	 * @param exchange 서버 웹 교환 객체
	 * @return 응답 완료 Mono 객체
	 */
	public static Mono<Void> handleForbiddenAccess(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
		return exchange.getResponse().setComplete();
	}

	/**
	 * 요청의 헤더에 JWT 클레임 정보를 추가하는 메서드입니다.
	 * <p>
	 * JWT 토큰에서 추출된 사용자 ID, 이메일, 역할 정보를 HTTP 요청 헤더에 추가하여,
	 * 이후 서비스 레이어에서 해당 정보를 사용할 수 있도록 합니다.
	 * 추가된 헤더는 X-User-Id, X-Email, X-Role로 전달됩니다.
	 *
	 * @param exchange 서버 교환 객체 (요청 및 응답 정보를 포함)
	 * @param claims JWT에서 추출한 클레임 정보 (사용자 ID, 이메일, 역할 등)
	 */
	private static void addHeadersToRequest(ServerWebExchange exchange, Map<String, Object> claims) {
		exchange.getRequest()
			.mutate()
			.header("X-User-Id", claims.get("sub").toString())   // 사용자 ID를 헤더에 추가
			.header("X-Email", claims.get("email").toString())   // 이메일을 헤더에 추가
			.header("X-Role", claims.get("role").toString())     // 역할 정보를 헤더에 추가
			.build();
	}
}