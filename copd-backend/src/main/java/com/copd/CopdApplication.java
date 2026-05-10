package com.copd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.copd.mapper")
public class CopdApplication {

    public static void main(String[] args) {
        SpringApplication.run(CopdApplication.class, args);
        System.out.println("========================================");
        System.out.println("  COPD系统启动成功!");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
