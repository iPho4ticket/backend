//package com.ticketing.gatewayservice.application.filter;
//
//import static com.ticketing.gatewayservice.infrastructure.helper.ArbitraryClaimsFactory.*;
//import static com.ticketing.gatewayservice.infrastructure.helper.ArbitraryExchangeFactory.*;
//import static org.hamcrest.MatcherAssert.*;
//import static org.hamcrest.Matchers.*;
//import static org.mockito.Mockito.*;
//
//import java.util.Map;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ServerWebExchange;
//
//import com.ticketing.gatewayservice.infrastructure.cache.CacheService;
//import com.ticketing.gatewayservice.infrastructure.feign.AuthWebClient;
//
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
///**
// * AuthTokenFilter의 주요 로직을 검증하는 테스트 클래스입니다.
// */
//class AuthTokenFilterTest {
//
//	@Mock
//	private AuthWebClient authWebClient;
//
//	@Mock
//	private CacheService cacheService;
//
//	@Mock
//	private GatewayFilterChain chain;
//
//	@InjectMocks
//	private AuthTokenFilter authTokenFilter;
//
//	private ServerWebExchange exchange;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//		exchange = anExchange();
//	}
//
//	/**
//	 * 캐시에 저장된 토큰을 사용하여 요청이 성공적으로 처리되는지 검증합니다.
//	 */
//	@Test
//	@DisplayName("캐시된_토큰이_있을때_요청이_성공적으로_처리")
//	void cachedToken_successfullyProcessesRequest() {
//		// Given: 캐시에 저장된 토큰 정보
//		Map<String, Object> cachedClaims = claims();
//
//		when(cacheService.getFromCache("mockToken")).thenReturn(cachedClaims);
//		when(chain.filter(exchange)).thenReturn(Mono.empty());
//
//		// When: 필터 실행
//		Mono<Void> result = authTokenFilter.filter(exchange, chain);
//
//		// Then: 필터 성공 검증
//		StepVerifier.create(result).verifyComplete();
//		assertThat(exchange.getResponse().getStatusCode(), is(HttpStatus.OK));
//	}
//
//	/**
//	 * 캐시 미스 시 AuthWebClient를 호출하고 성공적으로 처리되는지 검증합니다.
//	 */
//	@Test
//	@DisplayName("캐시미스_발생시_AuthWebClient_호출하여_성공적으로_처리")
//	void cacheMiss_triggersAuthWebClientCall_andSuccessfullyProcessesRequest() {
//		// Given: 캐시 미스 상황
//		when(cacheService.getFromCache("mockToken")).thenReturn(null);
//
//		// AuthWebClient가 성공적으로 토큰을 검증한 경우
//		Map<String, Object> claims = claims();
//
//		when(authWebClient.validateToken("mockToken")).thenReturn(Mono.just(claims));
//		when(chain.filter(exchange)).thenReturn(Mono.empty());
//
//		// When: 필터 실행
//		Mono<Void> result = authTokenFilter.filter(exchange, chain);
//
//		// Then: AuthWebClient 호출 후 성공 검증
//		StepVerifier.create(result).verifyComplete();
//		assertThat(exchange.getResponse().getStatusCode(), is(HttpStatus.OK));
//	}
//
//	/**
//	 * 유효하지 않은 토큰이 주어졌을 때 401 응답을 반환하는지 검증합니다.
//	 */
//	@Test
//	@DisplayName("유효하지_않은_토큰이_주어지면_401_응답_반환")
//	void invalidToken_returnsUnauthorized() {
//		// Given: 유효하지 않은 토큰
//		when(cacheService.getFromCache("mockToken")).thenReturn(null);
//		when(authWebClient.validateToken("mockToken")).thenReturn(
//			Mono.error(new RuntimeException("Unauthorized")));
//
//		// When: 필터 실행
//		Mono<Void> result = authTokenFilter.filter(exchange, chain);
//
//		// Then: 401 Unauthorized 상태가 반환되는지 검증
//		StepVerifier.create(result).verifyComplete();
//		assertThat(exchange.getResponse().getStatusCode(), is(HttpStatus.UNAUTHORIZED));
//	}
//}