package com.ticketing.gatewayservice.application.filter;

import static com.ticketing.gatewayservice.infrastructure.handler.AuthTokenResponseHandler.*;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Order(-1)
@Component
@NoArgsConstructor
@Slf4j
public class DenyAccessGatewayFilter extends AbstractGatewayFilterFactory<Object> {

	@Override
	public GatewayFilter apply(Object config) {
		return (exchange, chain) -> {
			log.info("Handling forbidden access");
			return handleForbiddenAccess(exchange);
		};
	}
}