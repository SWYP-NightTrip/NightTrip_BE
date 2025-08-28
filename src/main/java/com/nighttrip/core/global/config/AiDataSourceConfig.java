package com.nighttrip.core.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.nighttrip.core.ai",   // AI 전용 리포 위치
        entityManagerFactoryRef = "aiEntityManagerFactory",
        transactionManagerRef = "aiTransactionManager"
)
public class AiDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.ai")
    public DataSourceProperties aiDsProps() {
        return new DataSourceProperties();
    }

    @Bean(name = "aiDataSource")
    public DataSource aiDataSource(@Qualifier("aiDsProps") DataSourceProperties p) {
        // ── 디버깅용(원인 추적 시 임시): URL/ID 바인딩 확인
        System.out.println("[AI-DS] url=" + p.getUrl());
        System.out.println("[AI-DS] username=" + p.getUsername());

        return p.initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "aiEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean aiEmf(
            @Qualifier("aiDataSource") DataSource ds) {

        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        emf.setPackagesToScan(
                "com.nighttrip.core.domain",
                "com.nighttrip.core.global.image.entity"
        );      // 엔티티 패키지
        emf.setPersistenceUnitName("ai");                        // (권장) 구분용

        var vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(false);                            // ddl-auto는 props로 통제
        emf.setJpaVendorAdapter(vendor);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");             // 스키마 건드리지 않음
        props.put("hibernate.jdbc.lob.non_contextual_creation", true);
        props.put("hibernate.default_schema", "ai_base");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        props.put("hibernate.implicit_naming_strategy",
                "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");

        emf.setJpaPropertyMap(props);
        return emf;
    }

    @Bean(name = "aiTransactionManager")
    public PlatformTransactionManager aiTx(
            @Qualifier("aiEntityManagerFactory") EntityManagerFactory f) {
        return new JpaTransactionManager(f);
    }

    @Bean(name = "aiJdbcTemplate")
    public JdbcTemplate aiJdbcTemplate(@Qualifier("aiDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
