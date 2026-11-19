package com.mcpforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@MapperScan("com.mcpforge.core.mapper")
@SpringBootApplication
public class McpforgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpforgeApplication.class, args);
    }
}
