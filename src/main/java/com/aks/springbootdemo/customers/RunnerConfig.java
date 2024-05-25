package com.aks.springbootdemo.customers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RunnerConfig {

    @Bean
    ApplicationRunner runner(CustomerService customerService) {
        return args -> {
            log.info("customerService.class {}", customerService.getClass());
            log.info("customer added ==> {}", customerService.add("One"));
            log.info("customer added ==> {}", customerService.add("Two"));
            log.info("customer added ==> {}", customerService.add("Ankist"));

            customerService.getAll().forEach(customer -> log.info("customer ==> {}", customer));
        };
    }
}
