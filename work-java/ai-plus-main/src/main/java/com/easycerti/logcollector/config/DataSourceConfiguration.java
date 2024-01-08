package com.easycerti.logcollector.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/*
 * @ author      : 박경일
 * @ date        : 2023.03.27.mon
 * @ description : multiconnection pool configuration
 * @ beans :  
 *    - HikariConfig adminHikariConfig()
 *    - DataSource adminDatasource() throws Exception
 *    - JdbcTemplate adminJdbcTemplate( ) throws Exception
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    HikariConfig adminHikariConfig() {
        return new HikariConfig();
    }

    @Bean
    DataSource adminDatasource() throws Exception {
        return new HikariDataSource( adminHikariConfig() );
    }
    
    @Bean
    JdbcTemplate adminJdbcTemplate( ) throws Exception {
    	return new JdbcTemplate( adminDatasource() );
    }
}
