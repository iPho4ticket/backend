package com.ipho4ticket.paymentservice.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.ipho4ticket.paymentservice.infrastructure.repository")
public class JpaConfig {

}
