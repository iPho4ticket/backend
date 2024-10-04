package com.ticketing.authservice.application.service;

import static com.ticketing.authservice.infrastructure.helper.ArbitraryUserFactory.*;
import static com.ticketing.authservice.infrastructure.helper.util.ArbitraryField.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ticketing.authservice.application.dto.UserDto;
import com.ticketing.authservice.application.mapper.AuthDataMapper;
import com.ticketing.authservice.infrastructure.client.UserFeignService;
import com.ticketing.authservice.infrastructure.common.CustomException.PasswordMismatchException;
import com.ticketing.authservice.infrastructure.security.BCryptUtil;
import com.ticketing.authservice.infrastructure.security.JwtUtil;

/**
 * {@code AuthServiceTest}는 {@code AuthService}의 회원 인증과 관련된
 * 비즈니스 로직을 테스트하는 클래스입니다.
 */
class AuthServiceTest {

	@Mock
	private UserFeignService userFeignService; // FeignClient 모킹

	private BCryptUtil bCryptUtil; // 비밀번호 암호화 유틸리티 인스턴스
	private JwtUtil jwtUtil; // JWT 유틸리티 인스턴스
	private AuthDataMapper authDataMapper;

	@InjectMocks
	private AuthService authService; // AuthService에 의존성 주입

	/**
	 * 테스트 시작 전에 Mockito의 목 객체를 초기화합니다.
	 */
	@BeforeEach
	void setUp() {
		openMocks(this);
		this.bCryptUtil = new BCryptUtil(); // BCryptUtil 인스턴스 생성
		this.jwtUtil = new JwtUtil(JWT_SECRET, JWT_EXPIRATION); // JwtUtil 인스턴스 생성
		authService = new AuthService(bCryptUtil, authDataMapper, userFeignService, jwtUtil); // AuthService 인스턴스 생성
	}

	/**
	 * 유효한 사용자 인증 정보로 로그인할 때 JWT 토큰을 반환하는지 테스트합니다.
	 *
	 * @throws Exception 유효한 자격 증명이 있는 경우 발생할 수 있는 예외
	 */
	@Test
	@DisplayName("유효한 사용자 자격 정보로 로그인시 JWT 토큰 반환")
	void login_userWithValidCredentials_returnsJwtToken() throws Exception {
		// Given: 로그인 요청 DTO와 User 정보 생성
		UserDto.Auth.Login loginDto = createLoginDto();
		UserDto.Auth.Result fetchedUser = createAuthResult()
			.withPassword(bCryptUtil.hashPassword(PASSWORD));

		// Mock: FeignClient 호출 및 비밀번호 검증
		when(userFeignService.readUserByEmail(loginDto.email()))
			.thenReturn(fetchedUser);

		// When: 로그인 처리
		String token = authService.login(loginDto);

		// Then: JWT 토큰이 성공적으로 생성되었는지 확인
		assertThat(token, notNullValue());
		assertThat(jwtUtil.isTokenValid(token), is(true)); // 토큰 유효성 확인
	}

	/**
	 * 잘못된 비밀번호로 로그인 시도 시 {@code PasswordMismatchException}이
	 * 발생하는지 테스트합니다.
	 */
	@Test
	@DisplayName("로그인 요청시 비밀번호 불일치로 예외 발생")
	void login_userWithInvalidPassword_throwsPasswordMismatchException() {
		// Given: 로그인 요청 DTO와 잘못된 비밀번호 정보
		UserDto.Auth.Login loginDto = createLoginDto();
		UserDto.Auth.Result fetchedUser = createAuthResult()
			.withPassword(bCryptUtil.hashPassword("inVa1!rdPassw0rd"));

		// Mock: FeignClient 호출 및 잘못된 비밀번호 검증
		when(userFeignService.readUserByEmail(loginDto.email()))
			.thenReturn(fetchedUser);

		// When & Then: 비밀번호 불일치로 예외 발생
		assertThrows(PasswordMismatchException.class, () -> authService.login(loginDto));
		verify(userFeignService, times(1)).readUserByEmail(loginDto.email());
	}
}