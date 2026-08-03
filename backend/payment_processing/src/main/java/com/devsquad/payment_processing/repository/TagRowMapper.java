package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Tag;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class TagRowMapper implements RowMapper<Tag> {

    @Override
    public Tag mapRow(
            ResultSet resultSet,
            int rowNumber) throws SQLException {

        Integer tagId = resultSet.getInt("tag_id");
        String tagName = resultSet.getString("tag_name");
        String description = resultSet.getString("description");

        return new Tag(
                tagId,
                tagName,
                description
        );
    }
}