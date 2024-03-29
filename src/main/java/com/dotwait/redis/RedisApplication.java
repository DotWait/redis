package com.dotwait.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication
public class RedisApplication {


    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }

}
