/**
 * `UserEntityHelper` 클래스는 사용자 엔티티 생성을 위한 헬퍼 클래스입니다.
 * 이 클래스는 사용자 엔티티를 생성하고 초기화할 수 있는 메서드를 제공합니다.
 */
package com.ticketing.userservice.helper;

import static com.ticketing.userservice.helper.ArbitraryUserFactory.*;
import static com.ticketing.userservice.util.InitializationHelper.*;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ticketing.userservice.domain.User;
import com.ticketing.userservice.domain.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * TestUserEntityFactory 클래스는 테스트 환경에서 User 엔티티를 생성하고 데이터베이스에 저장하는 헬퍼 클래스입니다.
 * 이 클래스는 엔티티의 기본값을 사용하거나 초기화 함수를 통해 사용자 엔티티를 생성할 수 있는 메서드를 제공합니다.
 */
public class TestUserEntityFactory {

	// 사용자 레포지토리 의존성 주입
	private final UserRepository userRepository;

	/**
	 * 기본값으로 초기화된 사용자 엔티티를 생성합니다.
	 *
	 * @return 저장된 사용자 엔티티
	 */
	public User generateUser() {
		return generateUser(noInit());
	}

	/**
	 * 주어진 초기화 함수에 따라 사용자 엔티티를 생성하고 저장합니다.
	 *
	 * @param initialize 사용자 엔티티를 초기화하는 함수
	 * @return 저장된 사용자 엔티티
	 */
	public User generateUser(Function<? super User, ? extends User> initialize) {
		// 기본 사용자 엔티티 생성
		User user = aUser();

		// 주어진 초기화 함수 적용
		user = initialize.apply(user);

		// 사용자 엔티티 저장
		return userRepository.save(user);
	}
}