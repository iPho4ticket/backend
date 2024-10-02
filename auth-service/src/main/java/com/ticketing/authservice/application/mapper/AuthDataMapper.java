package com.ticketing.authservice.application.mapper;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ticketing.authservice.application.dto.UserDto;

/**
 * AuthDataMapper는 회원가입 및 로그인과 관련된 데이터를 변환하는 역할을 담당하는 매퍼입니다.
 */
@Component
public class AuthDataMapper {

	/**
	 * 회원가입 요청 데이터를 받아서 UserDto.Create로 변환합니다.
	 * 비밀번호 암호화는 람다로 전달받아 처리합니다.
	 *
	 * @param create 회원가입 요청 데이터를 담은 UserDto.Create
	 * @param passwordEncoder 비밀번호를 암호화하는 함수
	 * @return 변환된 UserDto.Create
	 */
	public UserDto.Create toCreateUserDto(UserDto.Create create, Function<String, String> passwordEncoder) {
		String hashedPassword = passwordEncoder.apply(create.password());
		return new UserDto.Create(
			create.name(),
			create.email(),
			hashedPassword,
			create.phoneNumber(),
			create.role()
		);
	}
}