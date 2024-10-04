package com.ticketing.authservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ticketing.authservice.application.dto.UserDto;

/**
 * UserFeignClient는 user-service와 통신하여 사용자 정보를 처리하는 클라이언트 인터페이스입니다.
 *
 * 서비스 이름과 URL은 환경 변수에서 가져옵니다.
 */
@FeignClient(name = "${user-service.name}", url = "${user-service.url}")
public interface UserFeignClient {

	/**
	 * user-service로 사용자 생성 요청을 처리하는 메서드입니다.
	 *
	 * @param create 생성할 사용자 정보가 담긴 DTO
	 * @return 생성된 사용자 정보가 담긴 UserDto.Result 객체
	 */
	@PostMapping("/internal/users")
	UserDto.Result createUser(@RequestBody UserDto.Create create);

	/**
	 * 이메일을 기반으로 사용자 정보를 조회하는 메서드입니다.
	 *
	 * @param email 조회할 사용자 이메일
	 * @return 조회된 사용자 정보가 담긴 UserDto.Result 객체
	 */
	@GetMapping("/internal/users")
	UserDto.Auth.Result readUserByEmail(@RequestParam("email") String email);
}