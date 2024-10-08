package com.ticketing.gatewayservice.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * WebFlux 기반의 보안 설정을 담당하는 설정 파일입니다.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	/**
	 * WebFlux 보안 필터 체인을 설정합니다.
	 * CSRF 보호를 비활성화합니다.
	 *
	 * @param http 서버 HTTP 보안 설정
	 * @return 보안 필터 체인
	 */
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http.csrf(ServerHttpSecurity.CsrfSpec::disable);  // CSRF 보호 비활성화
		return http.build();
	}
}