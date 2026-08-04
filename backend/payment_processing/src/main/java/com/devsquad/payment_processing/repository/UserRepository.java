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

        // Enrich each user with accounts + credit cards
        for (User user : databaseUsers) {
            user.setAccounts(getAccountNumbersByUserId(user.getUserId()));
            user.setCreditCards(getCreditCardsByUserId(user.getUserId()));
        }

        return new ArrayList<>(databaseUsers);
    }

    public User getUserById(Long userId) {

        String sql = """
                SELECT user_id, name, mobile
                FROM users
                WHERE user_id = ?
                """;



        User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);

        if (user != null) {
            user.setAccounts(getAccountNumbersByUserId(user.getUserId()));
            user.setCreditCards(getCreditCardsByUserId(user.getUserId()));
        }

        return user;
    }

    private ArrayList<Long> getAccountNumbersByUserId(Long userId) {
        String sql = """
                SELECT account_number
                FROM accounts
                WHERE user_id = ?
                """;

        List<Long> accountNumbers = jdbcTemplate.queryForList(sql, Long.class, userId);
        return new ArrayList<>(accountNumbers);
    }

    private ArrayList<String> getCreditCardsByUserId(Long userId) {
        String sql = """
                SELECT card_number
                FROM creditcards
                WHERE user_id = ?
                """;

        List<String> cardNumbers = jdbcTemplate.queryForList(sql, String.class, userId);
        return new ArrayList<>(cardNumbers);
    }
}
