package com.ipho4ticket.notificationservice.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.ipho4ticket.notificationservice.domain.repository")
public class JpaConfig {

}
