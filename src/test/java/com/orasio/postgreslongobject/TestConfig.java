package com.orasio.postgreslongobject;

/**
 * Created by spielerl on 20/11/2017.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {



    @Bean
    public TestUtil testUtil() {
        return new TestUtil();
    }
}