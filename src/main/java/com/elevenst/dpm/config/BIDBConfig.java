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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.lang.annotation.*;

@Configuration
@MapperScan(basePackages = "com.elevenst.dpm.repository", annotationClass = BIDBConfig.BIDB.class, sqlSessionFactoryRef="bidbSqlSessionFactory")
public class BIDBConfig {
    @Bean(name = "bidbDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.bidb.datasource")
    public DataSource bidbDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "bidbSqlSessionFactory")
    @Primary
    public SqlSessionFactory bidbSqlSessionFactory(@Qualifier("bidbDataSource") DataSource bidbDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(bidbDataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "bidbSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate bidbSqlSessionTemplate(SqlSessionFactory bidbSqlSessionFactory){
        return new SqlSessionTemplate(bidbSqlSessionFactory);
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface BIDB {
    }
}