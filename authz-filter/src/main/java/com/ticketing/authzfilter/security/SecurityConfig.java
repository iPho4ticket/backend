package com.ticketing.authzfilter.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ticketing.authzfilter.security.filter.RoleAuthorizationFilter;

import lombok.RequiredArgsConstructor;

/**
 * SecurityConfig
 *
 * Spring Security 설정 클래스입니다. HTTP 요청에 대한 보안 규칙을 정의하고,
 * 커스텀 인증 필터를 추가하는 등의 보안 설정을 처리합니다.
 */
@Configuration
@EnableMethodSecurity
@ComponentScan(basePackages = "com.ticketing.authzfilter")
@RequiredArgsConstructor
public class SecurityConfig {

	// 공개 경로를 상수로 분리
	private static final String[] PUBLIC_MATCHERS = {
		"/h2-console/**",
		"/actuator/**",
		"/api/v1/internal/**",
		"/api/v1/auth/**"
	};
	private static final String[] GET_ONLY_PUBLIC_MATCHERS = {
		"/api/v1/events",
		"/api/v1/events/search",
		"/api/v1/events/{event_id}"
	};
	private final RoleAuthorizationFilter roleAuthorizationFilter;

	/**
	 * 보안 필터 체인을 정의합니다.
	 * 각 URL 경로에 대한 접근 권한을 설정하고, 커스텀 인증 필터를 추가합니다.
	 *
	 * @param http HttpSecurity 객체
	 * @return SecurityFilterChain 설정된 보안 필터 체인
	 * @throws Exception 보안 설정 중 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CSRF 설정 명시적 정의
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(authz -> {
				configurePublicAccess(authz);  // 공개 경로 설정
				authz.anyRequest().authenticated();  // 나머지 요청은 인증 필요
			})
			.addFilterBefore(roleAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * 공개적으로 접근 가능한 경로를 설정하는 메서드입니다.
	 *
	 * @param authz HttpSecurity.authorizeRequests 객체
	 */
	private void configurePublicAccess(
		AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
		// GET 요청만 허용하는 경로 설정
		authz.requestMatchers(HttpMethod.GET, GET_ONLY_PUBLIC_MATCHERS).permitAll();
		// 모든 메서드에 대해 허용하는 경로 설정
		authz.requestMatchers(PUBLIC_MATCHERS).permitAll();
	}
}