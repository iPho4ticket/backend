package com.ticketing.userservice.application.service;

import static com.ticketing.userservice.application.dto.UserDto.*;
import static com.ticketing.userservice.application.dto.UserMapper.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketing.userservice.domain.User;
import com.ticketing.userservice.domain.repository.UserRepository;
import com.ticketing.userservice.domain.repository.helper.RepositoryHelper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final RepositoryHelper<User, Long> repositoryHelper;

	/**
	 * 새로운 유저을 생성하는 메서드
	 *
	 * @param createDto 유저 생성 정보
	 * @return 생성된 유저 결과
	 */
	@Transactional
	public Result createUser(Create createDto) {
		User user = entityFrom(createDto);

		return dtoFrom(userRepository.save(user));
	}

	/**
	 * 유저 ID로 특정 유저을 조회하는 메서드
	 *
	 * @param id 조회할 유저의 ID
	 * @return 조회된 유저 결과
	 */
	public Result readUser(Long id) {
		return dtoFrom(repositoryHelper.findOrThrowNotFound(id));
	}

	/**
	 * 모든 유저을 조회하는 내부 메서드 (관리자용)
	 *
	 * @param pageable 페이징 정보
	 * @return 페이징된 유저 결과
	 */
	public Page<Result> readUserAll(Pageable pageable) {
		return dtoFrom(userRepository.findAll(pageable));
	}

	/**
	 * 유저을 삭제하는 메서드
	 *
	 * @param id 삭제할 유저의 ID
	 */
	@Transactional
	public void deleteUser(Long id) {
		User user = repositoryHelper.findOrThrowNotFound(id);

		userRepository.delete(user);
	}

	/**
	 * 유저를 소프트 삭제하는 메서드
	 *
	 * @param deleteDto 소프트 삭제할 유저의 ID와 삭제자 정보를 포함한 DTO
	 * @return 소프트 삭제된 유저의 결과와 삭제자 정보
	 */
	@Transactional
	public Delete.Result softDeleteUser(Delete.Soft deleteDto) {
		User user = repositoryHelper.findOrThrowNotFound(deleteDto.id());
		user.softDelete(deleteDto.deleterId());
		return deleteDtoFrom(userRepository.save(user));
	}
}