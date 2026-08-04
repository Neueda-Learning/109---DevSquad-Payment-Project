package com.devsquad.payment_processing.repository;

import com.devsquad.payment_processing.model.Currency;
import com.devsquad.payment_processing.model.PaymentMode;
import com.devsquad.payment_processing.model.Tag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class CatalogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TagRowMapper tagRowMapper;

    @Autowired
    public CatalogRepository(JdbcTemplate jdbcTemplate, TagRowMapper tagRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.tagRowMapper = tagRowMapper;
    }

    // ── Currencies — read from static/currencies.json, no DB needed ──────────

    public List<Currency> getAllCurrencies() {
        try {
            InputStream is = new ClassPathResource("static/currencies.json").getInputStream();
            ObjectMapper mapper = new ObjectMapper();

            // JSON shape: [{ "country": "...", "currency": "USD", "symbol": "$" }, ...]
            List<Map<String, String>> raw = mapper.readValue(is, new TypeReference<>() {});

            return raw.stream().map(entry -> new Currency(
                    null,                        // no id in JSON
                    entry.get("country"),
                    entry.get("symbol"),
                    entry.get("currency")        // currency code as currencyName
            )).toList();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load currencies.json: " + e.getMessage(), e);
        }
    }

    // ── Payment modes — read from enum, no DB needed ──────────────────────────

    public List<PaymentMode> getPaymentModes() {
        return Arrays.asList(PaymentMode.values());
    }

    // ── Tags — DB backed ──────────────────────────────────────────────────────

    public List<Tag> getAllTags() {
        String sql = "SELECT tag_id, tag_name, description FROM tags";
        return jdbcTemplate.query(sql, tagRowMapper);
    }

    public Tag createTag(Tag tag) {
        String sql = "INSERT INTO tags (tag_name, description) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tag.getTagName());
            ps.setString(2, tag.getDescription());
            return ps;
        }, keyHolder);

        tag.setTagId(keyHolder.getKey().intValue());
        return tag;
    }

    public int deleteTag(Integer tagId) {
        String sql = "DELETE FROM tags WHERE tag_id = ?";
        return jdbcTemplate.update(sql, tagId);
    }
}