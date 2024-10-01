package com.ticketing.userservice.domain.repository.helper;

import com.ticketing.userservice.domain.auditing.BaseEntity;

import jakarta.persistence.EntityNotFoundException;

/**
 * 엔티티와 관련된 기본 레포지토리 헬퍼 인터페이스.
 *
 * @param <E> 엔티티 클래스, 반드시 {@link BaseEntity}를 확장해야 함
 * @param <K> 엔티티의 기본 키 타입
 */
public interface RepositoryHelper<E extends BaseEntity, K> {

	/**
	 * 주어진 기본 키로 엔티티를 조회하고, 찾지 못하면 예외를 던집니다.
	 *
	 * @param primaryKey 엔티티의 기본 키
	 * @return 조회된 엔티티, 존재하지 않을 경우 예외 발생
	 * @throws EntityNotFoundException 엔티티가 존재하지 않거나 삭제된 경우 발생하는 예외
	 */
	E findOrThrowNotFound(K primaryKey) throws EntityNotFoundException;
}