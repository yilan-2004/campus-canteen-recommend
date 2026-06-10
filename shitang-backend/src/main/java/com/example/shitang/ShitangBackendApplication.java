package com.example.shitang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.shitang.mapper")
public class ShitangBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShitangBackendApplication.class, args);
    }
}
