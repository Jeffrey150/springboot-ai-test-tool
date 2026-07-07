package com.testai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Test AI 后端启动类
 */
@SpringBootApplication
@EnableAsync
public class TestAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestAiApplication.class, args);
    }
}
