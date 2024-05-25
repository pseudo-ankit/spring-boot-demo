package com.aks.springbootdemo.customers;

import org.springframework.context.ApplicationEvent;

class CustomerCreatedEvent extends ApplicationEvent {

    public CustomerCreatedEvent(Customer source) {
        super(source);
    }

    @Override
    public Customer getSource() {
        return (Customer) super.getSource();
    }
}
