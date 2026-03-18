package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmDbStorage.class,
        GenreDbStorage.class,
        DirectorDbStorage.class,
        UserDbStorage.class,
        FriendshipDbStorage.class,
        MpaDbStorage.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        DirectorRowMapper.class,
        UserRowMapper.class,
        MpaRowMapper.class,
})

@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseJdbcTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected FilmDbStorage filmStorage;

    @Autowired
    protected GenreDbStorage genreStorage;

    @Autowired
    protected DirectorDbStorage directorStorage;

    @Autowired
    protected UserDbStorage userStorage;

    @Autowired
    protected FriendshipDbStorage friendshipStorage;

    @Autowired
    protected MpaDbStorage mpaStorage;

    @BeforeEach
    void cleanUp() {
        // Порядок важен из-за внешних ключей!
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }
}