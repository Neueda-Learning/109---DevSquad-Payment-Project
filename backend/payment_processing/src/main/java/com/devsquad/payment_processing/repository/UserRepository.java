package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRowMapper userRowMapper;

    public ArrayList<User> getAllUsers() {

        String sql = """
                SELECT user_id, name, mobile
                FROM users
                """;

        List<User> databaseUsers =
                jdbcTemplate.query(sql, userRowMapper);

        return new ArrayList<>(databaseUsers);
    }
    public User getUserById(Long userId) {

        String sql = """
                SELECT user_id, name, mobile
                FROM users
                WHERE user_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                userRowMapper,
                userId
        );
    }
}
