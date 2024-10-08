package com.ticketing.gatewayservice.infrastructure.feign;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.ticketing.gatewayservice.infrastructure.configuration.SystemConfigService;

import reactor.core.publisher.Mono;

/**
 * Auth 서버와 통신하여 JWT 토큰을 검증하는 WebClient입니다.
 */
@Component
public class AuthWebClient {

	private final WebClient webClient;
	private final String validateUri;

	/**
	 * AuthWebClient의 생성자입니다.
	 * @param systemConfigService 시스템 설정을 관리하는 서비스
	 */
	public AuthWebClient(SystemConfigService systemConfigService) {
		this.webClient = WebClient.builder()
			.baseUrl(systemConfigService.getAuthServerUrl())  // Auth 서버 URL 설정
			.build();
		this.validateUri = systemConfigService.getAuthValidateUri();  // URI 설정
	}

	/**
	 * JWT 토큰을 Auth 서버에 검증 요청합니다.
	 *
	 * @param token 검증할 JWT 토큰
	 * @return 검증된 클레임 정보 (Map)
	 */
	public Mono<Map<String, Object>> validateToken(String token) {
		return webClient.get()
			.uri(validateUri)
			.header("Authorization", token)
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<>() {
			});  // 클레임 정보 반환
	}
}