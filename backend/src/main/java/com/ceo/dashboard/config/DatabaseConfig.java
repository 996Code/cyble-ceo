package com.ceo.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ceo.dashboard.repository")
public class DatabaseConfig {
    // JPA 配置
}