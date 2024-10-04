package com.ticketing.authservice.infrastructure.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * BCryptUtil은 비밀번호를 해시화하고 검증하는 유틸리티 클래스입니다.
 */
@Component
public class BCryptUtil {

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * 비밀번호를 BCrypt 알고리즘으로 해시화하는 메서드입니다.
	 *
	 * @param password 원본 비밀번호
	 * @return 해시화된 비밀번호
	 */
	public String hashPassword(String password) {
		return passwordEncoder.encode(password);
	}

	/**
	 * 원본 비밀번호와 해시화된 비밀번호가 일치하는지 확인하는 메서드입니다.
	 *
	 * @param rawPassword 원본 비밀번호
	 * @param encodedPassword 해시화된 비밀번호
	 * @return 비밀번호가 일치하면 true, 그렇지 않으면 false
	 */
	public boolean checkPassword(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}
}