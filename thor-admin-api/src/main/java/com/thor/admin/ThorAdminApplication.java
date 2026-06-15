package com.thor.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.thor.common.mapper")
public class ThorAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThorAdminApplication.class, args);
        System.out.println("==================================================");
        System.out.println("  Thor Admin API 启动成功！端口: 8080");
        System.out.println("==================================================");
    }
}