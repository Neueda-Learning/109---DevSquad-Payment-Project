package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.PaymentMode;
import com.devsquad.payment_processing.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CatalogRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CurrencyRowMapper currencyRowMapper;

    @Autowired
    private TagRowMapper tagRowMapper;

    // Get all currencies
    public List<Currency> getAllCurrencies() {

        String sql = """
                SELECT *
                FROM currencies
                """;

        return jdbcTemplate.query(sql, currencyRowMapper);
    }

    // Get all payment modes
    public List<PaymentMode> getPaymentModes() {

        String sql = """
                SELECT payment_mode
                FROM payment_modes
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> PaymentMode.valueOf(rs.getString("payment_mode"))
        );
    }

    // Get all tags
    public List<Tag> getAllTags() {

        String sql = """
                SELECT *
                FROM tags
                """;

        return jdbcTemplate.query(sql, tagRowMapper);
    }

    // Create tag
    public Tag createTag(Tag tag) {

        String sql = """
                INSERT INTO tags(tag_name, description)
                VALUES (?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {

            PreparedStatement ps =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, tag.getTagName());
            ps.setString(2, tag.getDescription());

            return ps;

        }, keyHolder);

        tag.setTagId(keyHolder.getKey().intValue());

        return tag;
    }

    // Delete tag
    public void deleteTag(Integer tagId) {

        String sql = """
                DELETE FROM tags
                WHERE tag_id = ?
                """;

        jdbcTemplate.update(sql, tagId);
    }
}