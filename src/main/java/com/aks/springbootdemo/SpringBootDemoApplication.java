package com.aks.springbootdemo;

import lombok.extern.slf4j.Slf4j;
import org.postgresql.Driver;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class SpringBootDemoApplication {

    public static void main(String[] args) throws Exception {

        var applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(DataConfig.class);
        applicationContext.refresh();
        applicationContext.start();

        var customerService = applicationContext.getBean(CustomerService.class);

        log.info("customerService.class {}", customerService.getClass());
        log.info("customer added ==> {}", customerService.update("One"));
        log.info("customer added ==> {}", customerService.update("Two"));
        log.info("customer added ==> {}", customerService.update("Ankist"));

        customerService.getAll().forEach(customer -> log.info("customer ==> {}", customer));
    }
}


record Customer(int id, String name) {
}

@Service
@Transactional
@Slf4j
class CustomerService {
    private final JdbcTemplate template;
    private final TransactionTemplate tnxTemplate;

    private final RowMapper<Customer> customerRowMapper = (resultSet, rowNum) -> {
        var id = resultSet.getInt("id");
        var name = resultSet.getString("name");
        return new Customer(id, name);
    };

    CustomerService(JdbcTemplate template, TransactionTemplate tnxTemplate) {
        this.template = template;
        this.tnxTemplate = tnxTemplate;
    }

    Collection<Customer> getAll() throws Exception {
        return template.query("select * from customer", this.customerRowMapper);
    }

    Customer update(String name) {

        return this.tnxTemplate.execute(status -> {
            var arrayList = new ArrayList<Map<String, Object>>();
            arrayList.add(Map.of("id", Integer.class));
            var keyHolder = new GeneratedKeyHolder(arrayList);

            template.update(
                    con -> {
                        var ps = con.prepareStatement("""
                                insert into customer (name) values(?)
                                on conflict on constraint customer_name_key do update set name = excluded.name;
                                """, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, name);
                        return ps;
                    },
                    keyHolder
            );

            log.info("keyHolder : {}", keyHolder.getKeys());
            var generatedId = Objects.requireNonNull(keyHolder.getKeys()).get("id");
            Assert.state(generatedId instanceof Number, "the generated id should be a Number!");
            var id = ((Number) generatedId).intValue();
            // at this point the upsert statement already executed, but the transaction manager will roll back, and the entry will not be made
            // NOTE :- although the entry is not made, the id is already incremented as a side effect
            Assert.isTrue(!name.contains("Ankit"), "Ankit not allowed");
            return getById(id);
        });
    }

    Customer getById(Integer id) {
        return this.template.queryForObject("select * from customer where id = ?", this.customerRowMapper, id);
    }
}

@Configuration
@ComponentScan
@EnableTransactionManagement
class DataConfig {

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager tnxManager) {
        return new TransactionTemplate(tnxManager);
    }

    @Bean
    DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }


    @Bean
    JdbcTemplate template(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    DriverManagerDataSource driverManagerDataSource() {
        var dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://localhost/postgres",
                "postgres", "postgres"
        );
        dataSource.setDriverClassName(Driver.class.getName());
        return dataSource;
    }
}
