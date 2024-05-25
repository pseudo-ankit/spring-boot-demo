package com.aks.springbootdemo.customers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@RestController
@Slf4j
class CustomerController implements ApplicationListener<CustomerCreatedEvent> {

    Set<Customer> customers = new ConcurrentSkipListSet<>(Comparator.comparing(Customer::id));

    @GetMapping("/customers")
    Collection<Customer> getAllCustomers() {
        return customers;
    }

    @Override
    public void onApplicationEvent(CustomerCreatedEvent event) {
        log.info("customer created event received");
        customers.add(event.getSource());
    }
}
