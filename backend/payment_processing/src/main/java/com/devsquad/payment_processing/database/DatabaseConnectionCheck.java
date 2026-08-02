package com.devsquad.payment_processing.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionCheck implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        // prints confirmation message
        System.out.println("Database connection is successful. Result: " + result);
    }
}