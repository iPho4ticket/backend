package com.ticketing.authservice.presentation.controller;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.authservice.application.dto.UserDto;
import com.ticketing.authservice.application.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원가입 엔드포인트입니다.
	 *
	 * @param create 회원가입 요청 데이터를 담은 DTO
	 * @return 회원가입 결과를 담은 DTO
	 */
	@PostMapping("/signup")
	public ResponseEntity<UserDto.Result> signup(@RequestBody UserDto.Create create) {
		UserDto.Result result = authService.signup(create);
		return new ResponseEntity<>(result, CREATED);  // 201 Created
	}

	/**
	 * 로그인 엔드포인트입니다.
	 *
	 * @param login 로그인 요청 데이터를 담은 DTO
	 * @return JWT 토큰을 담은 응답
	 */
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody UserDto.Auth.Login login) {
		String token = authService.login(login);
		return new ResponseEntity<>(token, OK);  // 200 OK
	}
}