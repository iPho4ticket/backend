package com.ticketing.authservice.infrastructure.client;

import org.springframework.stereotype.Service;

import com.ticketing.authservice.application.dto.UserDto;

import lombok.RequiredArgsConstructor;

/**
 * UserFeignService는 Feign Client를 통해 user-service와 통신하는 로직을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class UserFeignService {

	private final UserFeignClient userFeignClient;

	/**
	 * user-service로 사용자 생성 요청을 보내는 메서드입니다.
	 *
	 * @param create 생성할 사용자 정보가 담긴 UserDto.Create
	 * @return 생성된 사용자 정보가 담긴 UserDto.Result
	 */
	public UserDto.Result createUser(UserDto.Create create) {
		return userFeignClient.createUser(create);
	}

	/**
	 * user-service로부터 사용자 정보를 조회하는 메서드입니다.
	 *
	 * @param email 조회할 사용자 ID
	 * @return 조회된 사용자 정보가 담긴 UserDto.Auth.Result
	 */
	public UserDto.Auth.Result readUserByEmail(String email) {
		return userFeignClient.readUserByEmail(email);
	}
}