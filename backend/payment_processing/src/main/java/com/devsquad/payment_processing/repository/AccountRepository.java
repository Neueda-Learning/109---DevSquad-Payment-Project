package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbcTemplate;
    private final AccountRowMapper accountRowMapper;

    @Autowired
    public AccountRepository(JdbcTemplate jdbcTemplate, AccountRowMapper accountRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountRowMapper = accountRowMapper;
    }

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

    /**
     * Debit amount from an account. Throws exception if insufficient balance.
     */
    public void debitAccount(Long accountNumber, Double amount) {
        String sql = """
                UPDATE accounts
                SET balance = balance - ?
                WHERE account_number = ?
                  AND balance >= ?
                  AND is_active = TRUE
                """;

        int rowsAffected = jdbcTemplate.update(sql, amount, accountNumber, amount);
        if (rowsAffected == 0) {
            throw new RuntimeException("INSUFFICIENT_BALANCE or ACCOUNT_INACTIVE: " + accountNumber);
        }
    }

    /**
     * Credit amount to an account.
     */
    public void creditAccount(Long accountNumber, Double amount) {
        String sql = """
                UPDATE accounts
                SET balance = balance + ?
                WHERE account_number = ?
                  AND is_active = TRUE
                """;

        int rowsAffected = jdbcTemplate.update(sql, amount, accountNumber);
        if (rowsAffected == 0) {
            throw new RuntimeException("ACCOUNT_NOT_FOUND or INACTIVE: " + accountNumber);
        }
    }

    /**
     * Get account balance. Returns null if account doesn't exist.
     */
    public Double getAccountBalance(Long accountNumber) {
        String sql = """
                SELECT balance
                FROM accounts
                WHERE account_number = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, Double.class, accountNumber);
        } catch (Exception e) {
            return null;
        }
    }
}
