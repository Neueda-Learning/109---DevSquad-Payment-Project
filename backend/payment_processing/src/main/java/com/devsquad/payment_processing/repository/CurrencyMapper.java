package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Currency;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CurrencyRowMapper implements RowMapper<Currency> {

    @Override
    public Currency mapRow(
            ResultSet resultSet,
            int rowNumber) throws SQLException {

        Integer currencyId = resultSet.getInt("currency_id");
        String currencyCountry = resultSet.getString("currency_country");
        String currencySymbol = resultSet.getString("currency_symbol");
        String currencyName = resultSet.getString("currency_name");

        return new Currency(
                currencyId,
                currencyCountry,
                currencySymbol,
                currencyName
        );
    }
}