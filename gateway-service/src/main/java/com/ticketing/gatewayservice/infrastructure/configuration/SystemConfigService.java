package com.ticketing.gatewayservice.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * 시스템 설정 값을 관리하는 서비스입니다.
 * 외부 YML 파일로부터 주입된 환경 변수를 사용하여
 * Auth 서버 URL 및 캐시 관련 설정을 관리합니다.
 */
@Getter
@Component
public class SystemConfigService {

	@Value("${auth-service.url}")
	private String authServerUrl;  // Auth 서버의 URL

	@Value("${auth-service.name}")
	private String authServerName;  // Auth 서버의 이름

	@Value("${cache.expiration:3600}")
	private long cacheExpiration;  // 캐시 만료 시간 (초 단위, 기본값 3600초)

	@Value("${cache.max-size:1000}")
	private long cacheMaxSize;  // 캐시 최대 크기 (기본값 1000)

	/**
	 * Auth 서버의 JWT 검증 URI를 반환합니다.
	 * authServerUrl에 "/validate" 경로를 추가하여 조합합니다.
	 *
	 * @return JWT 검증을 위한 URI
	 */
	public String getAuthValidateUri() {
		return authServerUrl + "/validate";
	}
}