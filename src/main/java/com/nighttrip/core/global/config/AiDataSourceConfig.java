package com.nighttrip.core.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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

@Configuration
@EnableJpaRepositories(
        basePackages = "com.nighttrip.core.global.ai.repository",   // ⬅️ AI 전용 리포 위치
        entityManagerFactoryRef = "aiEntityManagerFactory",
        transactionManagerRef   = "aiTransactionManager"
)
public class AiDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.ai")
    public DataSourceProperties aiDsProps() { return new DataSourceProperties(); }

    @Bean(name = "aiDataSource")
    public DataSource aiDataSource(@Qualifier("aiDsProps") DataSourceProperties p) {
        return p.initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "aiEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean aiEmf(
            @Qualifier("aiDataSource") DataSource ds) {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(ds);
        // ⬇️ 공용 엔티티 패키지(예: TouristSpot, ImageUrl 등) 포함
        emf.setPackagesToScan("com.nighttrip.core.domain");
        var vendor = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendor);
        var props = new HashMap<String, Object>();
        props.put("hibernate.hbm2ddl.auto", "none");                 // 스키마 건드리지 않음
        props.put("hibernate.jdbc.lob.non_contextual_creation", true);
        props.put("hibernate.default_schema", "rec");                // 기본은 뷰 스키마
        emf.setJpaPropertyMap(props);
        return emf;
    }

    @Bean(name = "aiTransactionManager")
    public PlatformTransactionManager aiTx(
            @Qualifier("aiEntityManagerFactory") EntityManagerFactory f) {
        return new JpaTransactionManager(f);
    }

    // 선택: JDBC도 쓰고 싶으면
    @Bean(name = "aiJdbcTemplate")
    public JdbcTemplate aiJdbcTemplate(@Qualifier("aiDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
