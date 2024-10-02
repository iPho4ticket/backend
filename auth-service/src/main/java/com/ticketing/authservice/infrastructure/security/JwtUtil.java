package com.ticketing.authservice.infrastructure.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JwtUtil은 JWT 토큰을 생성하고 검증하는 유틸리티 클래스입니다.
 */
@Component
public class JwtUtil {

	// 환경 변수에서 시크릿 키와 만료 시간 값을 주입받습니다.
	private final SecretKey secretKey;
	private final long jwtExpirationMs;

	// @Value 어노테이션을 통해 yml 파일에서 직접 SecretKey 및 만료 시간 설정
	public JwtUtil(
		@Value("${jwt.secret}") String secretKeyString,
		@Value("${jwt.expiration}") long jwtExpirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
		this.jwtExpirationMs = jwtExpirationMs;
	}

	/**
	 * 주어진 사용자 정보를 기반으로 JWT 토큰을 생성하는 메서드입니다.
	 *
	 * @param userId 사용자 ID
	 * @param email 사용자 이메일
	 * @param role 사용자 권한
	 * @return 생성된 JWT 토큰
	 */
	public String createToken(Long userId, String email, String role) {
		return Jwts.builder()
			.setSubject(String.valueOf(userId))
			.claim("email", email)
			.claim("role", role)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // 설정된 만료 시간 사용
			.signWith(secretKey, SignatureAlgorithm.HS256) // SecretKey와 알고리즘을 명시
			.compact();
	}

	/**
	 * JWT 토큰에서 클레임 정보를 추출하는 메서드입니다.
	 *
	 * @param token JWT 토큰
	 * @return 토큰에서 추출한 클레임
	 */
	public Claims extractClaims(String token) {
		return Jwts.parserBuilder() // parser() 대신 parserBuilder() 사용
			.setSigningKey(secretKey) // SecretKey를 사용해 서명 검증
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	/**
	 * 주어진 JWT 토큰의 유효성을 검증하는 메서드입니다.
	 *
	 * @param token 검증할 JWT 토큰
	 * @return 유효한 토큰이면 true, 그렇지 않으면 false
	 */
	public boolean isTokenValid(String token) {
		try {
			extractClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}