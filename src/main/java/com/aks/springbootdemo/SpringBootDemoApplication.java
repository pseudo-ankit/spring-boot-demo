package com.aks.springbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.Driver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

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
        customerService.add("One");
        customerService.add("Two");
        customerService.getAll().forEach(customer -> log.info("customer ==> {}", customer));
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

        Customer add(String name) {

            var arrayList = new ArrayList<Map<String, Object>>();
            arrayList.add(Map.of("id", Integer.class));
            var keyHolder = new GeneratedKeyHolder(arrayList);

            this.template.update(
                    con -> {
                        var ps = con.prepareStatement("insert into customer (name) values(?)", Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, name);
                        return ps;
                    },
                    keyHolder
            );

            log.info("keyHolder : {}", keyHolder.getKeys());
            var generatedId = Objects.requireNonNull(keyHolder.getKeys()).get("id");
            Assert.state(generatedId instanceof Number, "the generated id should be a Number!");
            var id = ((Number) generatedId).intValue();
            return getById(id);
        }

        Customer getById(Integer id) {
            return this.template.queryForObject("select * from customer where id = ?", this.customerRowMapper, id);
        }
    }

    record Customer(int id, String name) {
    }

}
