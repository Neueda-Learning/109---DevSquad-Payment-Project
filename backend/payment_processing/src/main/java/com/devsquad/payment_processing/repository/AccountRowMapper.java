package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Account;
import com.devsquad.payment_processing.model.AccountType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class AccountRowMapper implements RowMapper<Account> {

    @Override
    public Account mapRow(
            ResultSet resultSet,
            int rowNumber) throws SQLException {

        long accountNumber = resultSet.getLong("account_number");
        long userId = resultSet.getLong("user_id");
        String bankName = resultSet.getString("bank_name");
        AccountType accountType = AccountType.valueOf(resultSet.getString("account_type"));
        BigDecimal balance = resultSet.getBigDecimal("balance");
        String ifsc = resultSet.getString("ifsc");
        String bankAddressddress = resultSet.getString("bank_address");
        String country = resultSet.getString("country");
        boolean isActive = resultSet.getBoolean("is_active");
        String notActiveReason = resultSet.getString("not_active_reason");


        return new Account(
                accountNumber,
                userId,
                bankName,
                accountType,
                balance,
                ifsc,
                bankAddressddress,
                country,
                isActive,
                notActiveReason
        );
    }
}
