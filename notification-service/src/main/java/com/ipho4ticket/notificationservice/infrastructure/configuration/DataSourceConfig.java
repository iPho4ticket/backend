package com.ipho4ticket.notificationservice.infrastructure.configuration;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "storage.datasource.core")
    public HikariConfig HikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Profile("local")
    public HikariDataSource localCoreDataSource(@Qualifier("HikariConfig") HikariConfig config)
        throws SQLException {
        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9096").start();
        return new HikariDataSource(config);
    }
}
