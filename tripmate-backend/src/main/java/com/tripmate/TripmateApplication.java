package com.tripmate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tripmate.mapper")
public class TripmateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripmateApplication.class, args);
    }
}
