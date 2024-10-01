package com.ticketing.userservice.service;

import static com.ticketing.userservice.application.dto.UserDto.*;
import static com.ticketing.userservice.helper.ArbitraryUserFactory.*;
import static com.ticketing.userservice.util.ArbitraryField.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ticketing.userservice.application.service.UserService;
import com.ticketing.userservice.domain.User;
import com.ticketing.userservice.domain.repository.UserRepository;
import com.ticketing.userservice.helper.TestUserEntityFactory;
import com.ticketing.userservice.util.H2DbCleaner;

import jakarta.persistence.EntityNotFoundException;

@SpringBootTest
class UserServiceTests {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private TestUserEntityFactory testUserEntityFactory;

	@BeforeEach
	void setUp() throws SQLException {

		H2DbCleaner.clean(dataSource);
	}

	/**
	 * 유저 생성 관련 테스트 클래스
	 */
	@Nested
	class CreateUserTests {

		@Test
		void 유효한_데이터로_유저_생성시_성공() {
			// Given
			Create createUserDto = aUserCreateDto();

			// When
			Result result = userService.createUser(createUserDto);

			// Then
			assertNotNull(result);
			assertNotNull(result.id());
			assertEquals(USER_NAME, result.name());
			assertEquals(USER_EMAIL, result.email());
			assertEquals(USER_ROLE, result.role());
		}
	}

	/**
	 * 유저 조회 관련 테스트 클래스
	 */
	@Nested
	class ReadUserTests {

		@Test
		void 유효한_유저ID로_조회시_성공() {
			// Given
			User savedUser = testUserEntityFactory.generateUser();

			// When
			Result result = userService.readUser(savedUser.getId());

			// Then
			assertNotNull(result);
			assertEquals(USER_NAME, result.name());
			assertEquals(USER_EMAIL, result.email());
			assertEquals(USER_ROLE, result.role());
		}

		@Test
		void 존재하지_않는_유저ID로_조회시_실패() {
			// When & Then
			assertThrows(EntityNotFoundException.class, () -> {
				userService.readUser(USER_ID);
			});
		}
	}

	/**
	 * 유저 삭제 관련 테스트 클래스
	 */
	@Nested
	class DeleteUserTests {

		@Test
		void 유효한_유저ID로_유저_삭제시_성공() {
			// Given
			User savedUser = testUserEntityFactory.generateUser();

			// When
			userService.deleteUser(savedUser.getId());

			// Then
			assertThrows(EntityNotFoundException.class, () -> {
				userService.readUser(savedUser.getId());
			});
		}

		@Test
		void 존재하지_않는_유저ID로_삭제시_실패() {
			// When & Then
			assertThrows(EntityNotFoundException.class, () -> {
				userService.deleteUser(USER_ID);
			});
		}

		@Test
		void 유효한_유저ID로_유저_삭제_처리시_성공() {
			// Given
			User savedUser = testUserEntityFactory.generateUser();

			// When
			userService.softDeleteUser(
				aUserDeleteSoftDto()
					.withId(savedUser.getId())
					.withDeleterId(savedUser.getId())
			);

			// Then
			assertThrows(EntityNotFoundException.class, () -> {
				userService.readUser(savedUser.getId());
			});
		}

		@Test
		void 존재하지_않는_유저ID로_삭제_처리시_실패() {
			// When & Then
			assertThrows(EntityNotFoundException.class, () -> {
				userService.softDeleteUser(aUserDeleteSoftDto());
			});
		}
	}

	/**
	 * 모든 유저 조회 테스트 클래스 (관리자 여부에 따른 필터링)
	 */
	@Nested
	class ReadAllUsersTests {

		@Test
		void 모든_유저_조회시_성공() {
			// Given
			int totalUserCount = 5;
			for (int i = 0; i < totalUserCount; i++) {
				testUserEntityFactory.generateUser();
			}

			Pageable pageable = PageRequest.of(0, 10);

			// When
			Page<Result> resultPage = userService.readUserAll(pageable);

			// Then
			assertNotNull(resultPage);
			assertEquals(totalUserCount, resultPage.getTotalElements());
		}
	}
}