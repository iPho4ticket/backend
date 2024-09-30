package com.ticketing.userservice.domain.repository.helper;

import org.springframework.stereotype.Component;

import com.ticketing.userservice.domain.User;

import jakarta.persistence.EntityManager;

/**
 * User 엔티티에 대한 RepositoryHelper 구현체.
 */
@Component
public class UserRepositoryHelper extends AbstractRepositoryHelper<User, Long> {

	public UserRepositoryHelper(EntityManager em) {
		super(em);
	}
}