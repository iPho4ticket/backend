package com.ticketing.authservice.presentation.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.authservice.application.service.AuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * 내부 시스템에서 인증 관련 요청을 처리하는 컨트롤러입니다.
 * Auth 서버와의 통신을 통해 JWT 토큰을 검증하는 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("api/v1/internal/auth")
@RequiredArgsConstructor
public class AuthInternalController {

	private final AuthService authService;

	/**
	 * JWT 토큰을 검증하는 엔드포인트입니다.
	 * 요청 헤더에서 전달된 토큰을 Auth 서버에 전달하여 검증한 후, 검증된 클레임 정보를 반환합니다.
	 *
	 * @param token 검증할 JWT 토큰
	 * @return 검증된 클레임 정보
	 */
	@GetMapping("/validate")
	public Mono<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
		return authService.validateToken(token);
	}
}