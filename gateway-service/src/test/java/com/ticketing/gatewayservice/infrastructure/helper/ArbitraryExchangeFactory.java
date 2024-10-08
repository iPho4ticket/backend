package com.ticketing.gatewayservice.infrastructure.helper;

import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

/**
 * ServerWebExchange 객체 생성을 관리하는 헬퍼 클래스입니다.
 */
public class ArbitraryExchangeFactory {

	/**
	 * 테스트에 사용할 기본 ServerWebExchange 객체를 생성하는 헬퍼 메서드입니다.
	 *
	 * @return 생성된 ServerWebExchange 객체
	 */
	public static ServerWebExchange anExchange() {
		ServerWebExchange exchange = anExchange("/api/v1/test");
		exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, "Bearer mockToken").build();
		return exchange;
	}

	private static ServerWebExchange anExchange(String uri) {
		return MockServerWebExchange.from(MockServerHttpRequest.get(uri).build());
	}
}