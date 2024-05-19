package com.aks.springbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.Driver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class SpringBootDemoApplication {

    public static void main(String[] args) throws Exception {
        var dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://localhost/postgres",
                "postgres", "postgres"
        );
        dataSource.setDriverClassName(Driver.class.getName());

        var customerService = new DefaultCustomerService(dataSource);
        customerService.getAll().forEach(customer -> log.info("customer ==> {}",customer));
    }

    static class DefaultCustomerService {
        private final DataSource dataSource;

        DefaultCustomerService(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        Collection<Customer> getAll() throws Exception {
            var result = new ArrayList<Customer>();

            try (var connection = dataSource.getConnection()) {
                try (var statement = connection.createStatement()) {
                    try (var resultSet = statement.executeQuery("select * from customer")) {
                        while (resultSet.next()) {
                            var id = resultSet.getInt("id");
                            var name = resultSet.getString("name");
                            result.add(new Customer(id, name));
                        }
                    }
                }
            }
            return result;
        }
    }

    record Customer(int id, String name) {
    }

}
