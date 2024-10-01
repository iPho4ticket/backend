package com.ticketing.userservice.util;

import com.ticketing.userservice.infrastructure.common.RoleType;

public abstract class ArbitraryField {

	/**
	 * User 도메인
	 */

	/** User 엔티티의 ID */
	public static final Long USER_ID = 77L;

	/** User 삭제자의 엔티티의 ID */
	public static final Long DELETER_USER_ID = 66L;

	/** 사용자 이름 */
	public static final String USER_NAME = "John Doe";

	/** 사용자 전화번호 */
	public static final String USER_PHONE_NUMBER = "010-1234-5678";

	/** 사용자 비밀번호 */
	public static final String USER_PASSWORD = "!P@ssW0rd";

	/** 사용자 이메일 */
	public static final String USER_EMAIL = "john.doe@email.com";

	/** 사용자 권한 */
	public static final RoleType USER_ROLE = RoleType.USER;

	/** 관리자 권한 */
	public static final RoleType MANAGER_ROLE = RoleType.MANAGER;

	/** 마스터 권한 */
	public static final RoleType MASTER_ROLE = RoleType.MASTER;
}