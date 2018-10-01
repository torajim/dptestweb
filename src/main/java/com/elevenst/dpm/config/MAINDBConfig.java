package com.elevenst.dpm.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.lang.annotation.*;

@Configuration
@MapperScan(basePackages = "com.elevenst.dpm.repository", annotationClass = MAINDBConfig.MAINDB.class, sqlSessionFactoryRef="maindbSqlSessionFactory")
public class MAINDBConfig {
    @Bean(name = "maindbDataSource")
    @ConfigurationProperties(prefix = "spring.maindb.datasource")
    public DataSource maindbDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "maindbSqlSessionFactory")
    public SqlSessionFactory maindbSqlSessionFactory(@Qualifier("maindbDataSource") DataSource maindbDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(maindbDataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "maindbSqlSessionTemplate")
    public SqlSessionTemplate maindbSqlSessionTemplate(SqlSessionFactory maindbSqlSessionFactory){
        return new SqlSessionTemplate(maindbSqlSessionFactory);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface MAINDB {
    }
}
