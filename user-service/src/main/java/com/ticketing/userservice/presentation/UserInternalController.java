package com.ticketing.userservice.presentation;

import static com.ticketing.userservice.application.dto.UserDto.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticketing.userservice.application.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * 유저 관련 내부 API를 제공하는 컨트롤러 클래스입니다.
 *
 * - 새로운 유저 생성, 특정 유저 조회 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

	private final UserService userService;

	/**
	 * 새로운 유저을 생성하는 엔드포인트입니다.
	 *
	 * @param dto 유저 생성 정보를 담은 DTO 객체
	 * @return 생성된 유저 정보를 반환합니다.
	 */
	@PostMapping
	public Result createUser(@RequestBody Create dto) {
		return userService.createUser(dto);
	}

	/**
	 * 특정 유저를 조회하는 엔드포인트입니다.
	 *
	 * @param email 조회할 유저의 이메일
	 * @return 조회된 유저 정보를 반환합니다.
	 */
	@GetMapping
	public Auth.Result readUserByEmail(@RequestParam String email) {
		return userService.readUserByEmail(email);
	}

}