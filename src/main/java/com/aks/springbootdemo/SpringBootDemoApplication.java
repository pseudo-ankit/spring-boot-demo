package com.aks.springbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringBootDemoApplication {

    public static void main(String[] args) throws Exception {

        SpringApplication.run(SpringBootDemoApplication.class, args);
    }
}

