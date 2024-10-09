package com.ticketing.authservice.application.service;

import static com.ticketing.authservice.infrastructure.common.CustomException.*;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ticketing.authservice.application.dto.UserDto;
import com.ticketing.authservice.application.mapper.AuthDataMapper;
import com.ticketing.authservice.infrastructure.client.UserFeignService;
import com.ticketing.authservice.infrastructure.security.BCryptUtil;
import com.ticketing.authservice.infrastructure.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * AuthService는 회원가입 및 로그인과 관련된 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final BCryptUtil bCryptUtil;
	private final AuthDataMapper authDataMapper;
	private final UserFeignService userFeignService;
	private final JwtUtil jwtUtil;

	/**
	 * 회원가입 로직을 처리하는 메서드입니다.
	 *
	 * @param create 회원가입 요청 데이터를 담은 DTO
	 * @return 회원가입 결과를 담은 DTO
	 */
	public UserDto.Result signup(UserDto.Create create) {
		// UserDto.Create 생성 및 비밀번호 암호화 (암호화 로직 전달)
		UserDto.Create createUserDto = authDataMapper.toCreateUserDto(create, bCryptUtil::hashPassword);

		// Feign Client를 통해 user-service에 사용자 생성 요청
		return userFeignService.createUser(createUserDto);
	}

	/**
	 * 로그인 로직을 처리하는 메서드입니다.
	 *
	 * @param login 로그인 요청 데이터를 담은 DTO
	 * @return 로그인 성공 시 JWT 토큰
	 */
	public String login(UserDto.Auth.Login login) {
		// Feign Client를 통해 user-service에서 사용자 정보 조회
		UserDto.Auth.Result user = userFeignService.readUserByEmail(login.email());

		// 비밀번호 확인 (BCryptUtil 사용)
		if (!bCryptUtil.checkPassword(login.password(), user.password())) {
			// 비밀번호 불일치 시 PasswordMismatchException 예외를 던집니다
			throw new PasswordMismatchException(user.id());
		}

		// JWT 토큰 생성
		return jwtUtil.createToken(user.id(), user.email(), user.role().getAuthority());
	}

	/**
	 * JWT 토큰을 검증하는 메서드입니다.
	 * 전달된 JWT 토큰의 유효성을 확인하고, 유효한 경우 해당 토큰의 클레임 정보를 반환합니다.
	 *
	 * @param token 검증할 JWT 토큰
	 * @return 토큰이 유효한 경우 클레임 정보를 포함한 Map을 반환하고,
	 *         유효하지 않을 경우 Mono.error를 반환합니다.
	 */
	public Mono<Map<String, Object>> validateToken(String token) {
		// JWT 유효성 검증
		if (!jwtUtil.isTokenValid(token)) {
			// 토큰이 유효하지 않은 경우 InvalidTokenException을 던집니다
			return Mono.error(new InvalidTokenException(token));
		}

		// 유효한 토큰일 경우, 토큰에서 클레임 정보를 추출합니다
		Map<String, Object> claims = jwtUtil.extractClaims(token);

		// 추출한 클레임 정보를 Mono로 감싸서 반환합니다
		return Mono.just(claims);
	}
}