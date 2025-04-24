package com.cq.RssHub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cq.RssHub.mapper")
@SpringBootApplication
public class RssHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(RssHubApplication.class, args);

    }

}
