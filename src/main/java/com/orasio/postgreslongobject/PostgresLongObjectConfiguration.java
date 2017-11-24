package com.orasio.postgreslongobject;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by spielerl on 18/11/2017.
 */
@Configuration
public class PostgresLongObjectConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    private Environment environment;

    @PostConstruct
    public void reCreateDBSchema() {
        try {
            String username = environment.getProperty("spring.datasource.username");
            String password = environment.getProperty("spring.datasource.password");
            String jdbcUrl = environment.getProperty("spring.datasource.url");
            int lastIndexOf = jdbcUrl.lastIndexOf("/");
            String schema = jdbcUrl.substring(lastIndexOf +1);
            Connection con = DriverManager.getConnection(jdbcUrl.substring(0, lastIndexOf)+"/postgres", username, password);
            // Creating a database schema
            Statement sta = con.createStatement();
            //in case exist
            try{
                sta.executeUpdate("DROP DATABASE "+ schema);
                System.out.println("Schema dropped:"+schema);
            }catch (Exception exp){}

            sta.executeUpdate("CREATE DATABASE "+schema);
            System.out.println("Schema created:"+schema);
            sta.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
//            e.printStackTrace();
        }
    }
    @Bean
    public SpringLiquibase liquibase(@SuppressWarnings("SpringJavaAutowiringInspection") @Autowired DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:liquibase/hibernate-changelog.xml");
        liquibase.setContexts("development, production");
        return liquibase;
    }



    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(-1);
        configurer.setTaskExecutor(asyncTaskExecutor());
    }

    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new SimpleAsyncTaskExecutor("async");
    }



}
