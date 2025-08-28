package com.nighttrip.core.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.nighttrip.core.domain",         // ✅ 메인 리포 범위
        entityManagerFactoryRef = "entityManagerFactory",   // 부트 기본 EMF
        transactionManagerRef   = "transactionManager"      // 부트 기본 Tx
)
public class MainJpaConfig { }