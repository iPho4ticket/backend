//package com.ticketing.gatewayservice.infrastructure.cache;
//
//import static com.ticketing.gatewayservice.infrastructure.helper.ArbitraryClaimsFactory.*;
//import static org.junit.jupiter.api.Assertions.*;
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
//
//import com.github.benmanes.caffeine.cache.Cache;
//
///**
// * CaffeineCacheService의 캐시 저장 및 조회 로직을 검증하는 테스트 클래스입니다.
// */
//class CaffeineCacheServiceTest {
//
//	@Mock
//	private Cache<String, Map<String, Object>> localCache;
//
//	@InjectMocks
//	private CaffeineCacheService caffeineCacheService;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//	}
//
//	/**
//	 * 캐시에 저장된 토큰 클레임 정보를 성공적으로 반환하는지 검증합니다.
//	 */
//	@Test
//	@DisplayName("캐시에서_토큰_클레임_정보_성공적으로_반환")
//	void cachedClaims_areSuccessfullyReturned_fromCache() {
//		// Given: 캐시에 저장된 클레임 정보
//		Map<String, Object> cachedClaims = claims();
//
//		when(localCache.getIfPresent("mockToken")).thenReturn(cachedClaims);
//
//		// When: 캐시에서 토큰을 조회했을 때
//		Map<String, Object> result = caffeineCacheService.getFromCache("mockToken");
//
//		// Then: 캐시된 클레임 정보를 반환하는지 검증
//		assertEquals(cachedClaims, result);
//	}
//
//	/**
//	 * 토큰 클레임 정보를 캐시에 성공적으로 저장하는지 검증합니다.
//	 */
//	@Test
//	@DisplayName("클레임_정보를_캐시에_성공적으로_저장")
//	void tokenClaims_areSuccessfullyStored_inCache() {
//		// Given: 저장할 클레임 정보
//		Map<String, Object> claims = claims();
//
//		// When: 클레임 정보를 캐시에 저장했을 때
//		caffeineCacheService.putInCache("mockToken", claims);
//
//		// Then: 캐시에 저장되었는지 검증
//		verify(localCache).put("mockToken", claims);
//	}
//}