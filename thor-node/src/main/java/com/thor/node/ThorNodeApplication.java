package com.thor.node;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.thor.common.mapper")
public class ThorNodeApplication {

    private static final Logger log = LoggerFactory.getLogger(ThorNodeApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ThorNodeApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
        log.info("Thor (雷神) Distributed Node initialized successfully in Non-Web mode.");
    }
}