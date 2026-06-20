package com.plansystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 日/月计划和成果验收系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.plansystem.mapper")
public class PlanSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanSystemApplication.class, args);
    }
}
