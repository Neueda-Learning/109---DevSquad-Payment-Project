package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNumber) throws SQLException {

        Long userId = resultSet.getLong("user_id");
        String name = resultSet.getString("name");
        String mobile = resultSet.getString("mobile");

        return new User(
                userId,
                name,
                mobile
        );
    }

}
