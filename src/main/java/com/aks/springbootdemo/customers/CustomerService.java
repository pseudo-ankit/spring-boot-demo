package com.aks.springbootdemo.customers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@Slf4j
class CustomerService {
    private final JdbcTemplate template;
    private final TransactionTemplate tnxTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private final RowMapper<Customer> customerRowMapper = (resultSet, rowNum) -> {
        var id = resultSet.getInt("id");
        var name = resultSet.getString("name");
        return new Customer(id, name);
    };

    CustomerService(
            JdbcTemplate template,
            TransactionTemplate tnxTemplate,
            ApplicationEventPublisher eventPublisher
    ) {
        this.template = template;
        this.tnxTemplate = tnxTemplate;
        this.eventPublisher = eventPublisher;
    }

    Collection<Customer> getAll() throws Exception {
        return template.query("select * from customer", this.customerRowMapper);
    }

    Customer add(String name) {

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
            var customer = getById(id);
            eventPublisher.publishEvent(new CustomerCreatedEvent(customer));
            return customer;
        });
    }

    Customer getById(Integer id) {
        return this.template.queryForObject("select * from customer where id = ?", this.customerRowMapper, id);
    }
}
