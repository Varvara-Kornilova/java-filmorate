package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestDataSqlLoaded {

    private final JdbcTemplate jdbcTemplate;

    @Test
    public void testDataSqlLoaded() {
        Integer mpaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa_rating", Integer.class);
        Integer genreCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM genres", Integer.class);

        assertThat(mpaCount).isEqualTo(5);
        assertThat(genreCount).isEqualTo(6);
    }
}