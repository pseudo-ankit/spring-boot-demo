package com.aks.springbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SpringBootDemoApplication {

    public static void main(String[] args) throws Exception {
        var dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://localhost/postgres",
                "postgres", "postgres"
        );
        dataSource.setDriverClassName(Driver.class.getName());

        var jdbcTemplate = new JdbcTemplate(dataSource);

        var customerService = new DefaultCustomerService(jdbcTemplate);
        customerService.getAll().forEach(customer -> log.info("customer ==> {}",customer));
    }

    static class DefaultCustomerService {
        private final JdbcTemplate template;

        private final RowMapper<Customer> customerRowMapper = (resultSet, rowNum) -> {
            var id = resultSet.getInt("id");
            var name = resultSet.getString("name");
            return new Customer(id, name);
        };

        DefaultCustomerService(JdbcTemplate jdbcTemplate) {
            this.template = jdbcTemplate;
        }

        Collection<Customer> getAll() throws Exception {
            return template.query("select * from customer", this.customerRowMapper);
        }
    }

    record Customer(int id, String name) {
    }

}
