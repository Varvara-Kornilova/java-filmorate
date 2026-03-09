package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public abstract class BaseDbStorage<T> {

    protected final JdbcTemplate jdbcTemplate;
    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    protected final RowMapper<T> rowMapper;

    public BaseDbStorage(JdbcTemplate jdbcTemplate, RowMapper<T> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.rowMapper = rowMapper;
    }

    protected T queryForObject(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, rowMapper, args);
    }

    protected List<T> queryForList(String sql, Object... args) {
        return jdbcTemplate.query(sql, rowMapper, args);
    }

    protected Long insertAndGetId(String sql, Object... args) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        }
        throw new RuntimeException("Не удалось получить сгенерированный ID");
    }

    protected int executeUpdate(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    protected boolean exists(String sql, Long id) {
        return jdbcTemplate.queryForObject(sql, Boolean.class, id);
    }

    protected Optional<T> findOptional(String sql, Object... args) {
        try {
            return Optional.ofNullable(queryForObject(sql, args));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}