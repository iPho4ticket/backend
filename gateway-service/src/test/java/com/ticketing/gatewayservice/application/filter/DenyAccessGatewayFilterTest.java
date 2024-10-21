//package com.ticketing.gatewayservice.application.filter;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.test.web.reactive.server.WebTestClient;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//public class DenyAccessGatewayFilterTest {
//
//	private WebTestClient webTestClient;
//
//	@LocalServerPort  // @SpringBootTest가 정의한 포트를 주입받습니다.
//	private int port;
//
//	@BeforeEach
//	void setUp() {
//		// 테스트 클라이언트를 수동으로 생성
//		this.webTestClient = WebTestClient.bindToServer()
//			.baseUrl("http://localhost:" + port)  // 로컬호스트와 지정된 포트를 이용해 테스트 클라이언트 생성
//			.build();
//	}
//
//	/**
//	 * DenyAccessGatewayFilter가 적용된 경로로 접근 시 403 Forbidden 응답을 반환하는지 테스트합니다.
//	 */
//	@Test
//	@DisplayName("DenyAccessGatewayFilter가 적용된 경로에서 403 응답 테스트")
//	void testDenyAccessFilter() {
//		webTestClient.get()
//			.uri("/api/v1/internal/test")  // DenyAccessGatewayFilter가 적용된 경로
//			.exchange()
//			.expectStatus().isForbidden();  // 403 응답 확인
//	}
//}