package com.ticketing.userservice.domain.repository.helper;

import static java.util.Optional.*;

import java.lang.reflect.ParameterizedType;

import com.ticketing.userservice.domain.auditing.BaseEntity;
import com.ticketing.userservice.infrastructure.common.codes.ErrorCode;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 엔티티와 관련된 기본 레포지토리 헬퍼 추상 클래스.
 *
 * @param <E> 엔티티 클래스, 반드시 {@link BaseEntity}를 확장해야 함
 * @param <K> 엔티티의 기본 키 타입
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractRepositoryHelper<E extends BaseEntity, K>
	implements RepositoryHelper<E, K> {

	private final EntityManager em;

	/**
	 * 주어진 기본 키로 엔티티를 조회하고, 존재하지 않거나 삭제된 경우 예외를 발생시킵니다.
	 *
	 * @param primaryKey 엔티티의 기본 키
	 * @return 조회된 엔티티, 존재하지 않으면 예외 발생
	 * @throws EntityNotFoundException 엔티티가 존재하지 않거나 삭제된 경우 예외 발생
	 */
	public E findOrThrowNotFound(K primaryKey) {
		Class<E> clazz = getGenericClass();
		return ofNullable(em.find(clazz, primaryKey))
			.filter(entity -> !entity.getIsDeleted())
			.orElseThrow(() -> createException(clazz, primaryKey));
	}

	/**
	 * 주어진 기본 키로 엔티티가 존재하는지 여부를 확인합니다.
	 *
	 * @param primaryKey 엔티티의 기본 키
	 * @return 엔티티가 존재하면 true, 존재하지 않으면 false
	 */
	public boolean existsById(K primaryKey) {
		Class<E> clazz = getGenericClass();
		String jpql = String.format("SELECT COUNT(e) FROM %s e WHERE e.id = :id AND e.isDeleted = false",
			clazz.getSimpleName());
		Long count = em.createQuery(jpql, Long.class)
			.setParameter("id", primaryKey)
			.getSingleResult();
		return count > 0;
	}

	/**
	 * 엔티티가 존재하지 않으면 예외를 발생시킵니다.
	 *
	 * @param primaryKey 엔티티의 기본 키
	 * @throws EntityNotFoundException 엔티티가 존재하지 않으면 예외 발생
	 */
	public void ensureExistsOrThrow(K primaryKey) {
		if (!existsById(primaryKey)) {
			Class<E> clazz = getGenericClass();
			throw createException(clazz, primaryKey);
		}
	}

	/**
	 * 리플렉션을 사용하여 제네릭 타입의 엔티티 클래스를 추출하여 반환합니다.
	 *
	 * @return 엔티티의 클래스 타입
	 */
	@SuppressWarnings("unchecked")
	private Class<E> getGenericClass() {
		ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
		return (Class<E>)parameterizedType.getActualTypeArguments()[0];
	}

	/**
	 * 주어진 엔티티 정보와 기본 키를 바탕으로 새로운 예외를 생성합니다.
	 *
	 * @param clazz      엔티티의 클래스 타입
	 * @param primaryKey 엔티티의 기본 키
	 * @return 상태와 메시지를 포함한 EntityNotFoundException
	 */
	private EntityNotFoundException createException(Class<E> clazz, K primaryKey) {
		return new EntityNotFoundException(
			String.format("%s with id %s: %s", clazz.getSimpleName(), primaryKey, ErrorCode.NOT_FOUND.getMessage())
		);
	}
}