package com.ticketing.userservice.infrastructure.common;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RoleType {

	USER(Authority.USER),
	MANAGER(Authority.MANAGER),
	MASTER(Authority.MASTER);

	private final String authority;

	/**
	 * roleCode에 따른 RoleType을 반환
	 *
	 * @param roleCode 전달 받은 권한(Role_권한)형식
	 * @return RoleType
	 */
	public static RoleType fromRoleCode(String roleCode) {
		for (RoleType role : RoleType.values()) {
			if (role.authority.equalsIgnoreCase(roleCode)) {
				return role;
			}
		}
		throw new IllegalArgumentException("Invalid role code: " + roleCode);
	}

	public String getAuthority() {
		return this.authority;
	}

	public static class Authority {
		public static final String USER = "ROLE_USER";
		public static final String MANAGER = "ROLE_MANAGER";
		public static final String MASTER = "ROLE_MASTER";
	}

}

