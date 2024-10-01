package com.ticketing.userservice.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class H2DbCleaner {

	// 시스템 테이블 스키마 정보 정의
	private static final String SYSTEM_CATALOG_SCHEMA = "INFORMATION_SCHEMA";
	private static final String SYSTEM_SCHEMA_PREFIX = "SYS";  // SYS로 시작하는 테이블도 시스템 테이블로 간주

	/**
	 * 데이터베이스 테이블을 정리하는 메서드. 참조 무결성을 임시로 해제하고,
	 * 사용자 테이블을 모두 TRUNCATE한 후 다시 무결성을 활성화함.
	 *
	 * @param dataSource 데이터 소스 (DB 연결을 위한 객체)
	 * @throws SQLException SQL 처리 중 예외 발생 시 던짐
	 */
	public static void clean(DataSource dataSource) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);

			// 참조 무결성 제약 해제
			setReferentialIntegrity(connection, false);

			// 모든 사용자 테이블을 순회하면서 TRUNCATE 실행
			for (String table : getClearingTables(connection)) {
				try {
					truncateTable(connection, table);
				} catch (SQLException e) {
					// TRUNCATE 실패 시 예외를 무시하고 넘어가기
					System.out.println("Error truncating table: " + table + ", skipping...");
				}
			}

			// 참조 무결성 제약 다시 활성화
			setReferentialIntegrity(connection, true);

			connection.commit();
		}
	}

	/**
	 * 참조 무결성을 활성화/비활성화하는 메서드.
	 *
	 * @param connection 데이터베이스 연결
	 * @param value 참조 무결성 활성화 여부
	 * @throws SQLException SQL 처리 중 예외 발생 시 던짐
	 */
	private static void setReferentialIntegrity(Connection connection, boolean value) throws SQLException {
		String sql = String.format("SET REFERENTIAL_INTEGRITY %s", value ? "TRUE" : "FALSE");
		connection.prepareStatement(sql).execute();
	}

	/**
	 * 지정된 테이블을 TRUNCATE하는 메서드.
	 *
	 * @param connection 데이터베이스 연결
	 * @param table TRUNCATE할 테이블 이름
	 * @throws SQLException SQL 처리 중 예외 발생 시 던짐
	 */
	private static void truncateTable(Connection connection, String table) throws SQLException {
		// 테이블 이름을 큰따옴표로 감싸서 예약어 문제와 대소문자 문제 해결
		String sql = String.format("TRUNCATE TABLE \"%s\"", table);
		connection.prepareStatement(sql).execute();
	}

	/**
	 * 시스템 테이블을 제외한 사용자 테이블 목록을 가져오는 메서드.
	 *
	 * @param connection 데이터베이스 연결
	 * @return 사용자 테이블 목록
	 * @throws SQLException SQL 처리 중 예외 발생 시 던짐
	 */
	private static List<String> getClearingTables(Connection connection) throws SQLException {
		DatabaseMetaData metaData = connection.getMetaData();  // 메타데이터를 가져옴
		ResultSet rs = metaData.getTables(null, null, "%", new String[] {"TABLE"});  // 테이블 목록을 조회

		List<String> tables = new ArrayList<>();
		while (rs.next()) {
			String schema = rs.getString("TABLE_SCHEM");  // 테이블 스키마 이름
			String table = rs.getString("TABLE_NAME");    // 테이블 이름

			// 시스템 테이블(INFORMATION_SCHEMA 및 SYS 스키마)을 제외
			if (!schema.equals(SYSTEM_CATALOG_SCHEMA) && !schema.startsWith(SYSTEM_SCHEMA_PREFIX)) {
				tables.add(String.format("%s.%s", schema, table));  // 사용자 테이블 목록에 추가
			}
		}
		return tables;  // 사용자 테이블 목록 반환
	}
}