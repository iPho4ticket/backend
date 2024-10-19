package com.ticketing.authzfilter.security;

import static com.ticketing.authzfilter.infrastructure.common.RoleType.Authority.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * RoleHierarchyConfig
 *
 * 역할 간의 계층 구조를 정의하는 설정 클래스입니다.
 * 상위 역할이 하위 역할을 포함하는 권한 계층을 설정할 수 있습니다.
 */
@Configuration
public class RoleHierarchyConfig {

	/**
	 * 역할 계층을 정의하는 RoleHierarchy 객체를 생성합니다.
	 *
	 * <p> @return RoleHierarchy 역할 계층을 정의한 객체
	 */
	@Bean
	public RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.fromHierarchy(
			MASTER + " > " + MANAGER + " \n" +
				MANAGER + " > " + USER
		);
	}
}