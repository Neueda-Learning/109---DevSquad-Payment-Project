package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AccountRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccountRowMapper accountRowMapper;

    public ArrayList<Account> getAllAccountsR() {

        String sql = """
                SELECT account_number, user_id, bank_name, account_type, balance, ifsc, bank_address, country, is_active, not_active_reason
                FROM accounts
               """;

        List<Account> databaseAccounts =
                jdbcTemplate.query(sql, accountRowMapper);

        return new ArrayList<>(databaseAccounts);
    }

    public Account getAccountByIdR(Long accountNumber) {

        String sql = """
                SELECT account_number, user_id, bank_name, account_type, balance, ifsc, bank_address, country, is_active, not_active_reason
                FROM accounts
                WHERE account_number = ?
               """;

        return jdbcTemplate.queryForObject(sql, accountRowMapper , accountNumber);
    }
}
