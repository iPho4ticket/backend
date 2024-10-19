package com.ticketing.authzfilter.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ticketing.authzfilter.infrastructure.common.RoleType;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * RoleAuthorizationFilter
 * <p>
 * 사용자 요청 시 헤더 정보를 바탕으로 인증을 수행하는 필터입니다.
 * 이 필터는 사용자의 ID, 이메일, 역할 정보를 헤더에서 추출한 후, Spring Security의
 * 인증 객체를 생성하고 이를 SecurityContext에 저장합니다.
 */
@Component
@Slf4j
public class RoleAuthorizationFilter extends OncePerRequestFilter {

	/**
	 * 인증 필터 내부 처리 메서드.
	 * <p>
	 * 이 메서드는 매 요청마다 실행되며, HTTP 헤더에서 사용자 ID, 이메일, 역할 정보를 추출하여
	 * Spring Security 인증 객체를 생성합니다. 이후 생성된 인증 객체를 SecurityContext에 저장하고,
	 * 다음 필터로 요청을 넘깁니다.
	 *
	 * @param request  클라이언트의 HTTP 요청 객체
	 * @param response 서버의 HTTP 응답 객체
	 * @param filterChain 필터 체인
	 * @throws ServletException 요청 처리 중 발생하는 Servlet 예외
	 * @throws IOException      입출력 예외 발생 시
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			// HTTP 헤더에서 사용자 ID, 이메일, 역할(role)을 추출
			String userId = request.getHeader("X-User-Id");
			String email = request.getHeader("X-Email");
			String roleCode = request.getHeader("X-Role");

			// 로그 추가: 헤더 정보 출력
			log.info("Received headers - X-User-Id: {}, X-Email: {}, X-Role: {}", userId, email, roleCode);

			// 사용자 ID, 이메일, 역할 정보가 존재할 경우
			if (userId != null && email != null && roleCode != null) {
				RoleType roleType = RoleType.fromRoleCode(roleCode);
				List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleType.getAuthority()));

				// 로그 추가: 권한 확인
				log.info("User role resolved to: {}", roleType.getAuthority());

				// 인증 객체 생성
				UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(userId, email, authorities);

				// SecurityContext에 인증 객체 저장
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);

				// 로그 추가: 인증 객체 저장 확인
				log.info("Authentication set in SecurityContext: {}",
					SecurityContextHolder.getContext().getAuthentication());
			} else {
				log.warn("Missing required headers for authentication");
			}

		} catch (Exception e) {
			log.error("Error during authentication process: ", e);
		}

		// 다음 필터로 요청을 전달
		filterChain.doFilter(request, response);
	}
}