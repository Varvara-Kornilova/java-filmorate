package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        GenreDbStorage.class,
        UserDbStorage.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        UserRowMapper.class
})
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    public void testCreateFilmWithoutGenres() {
        Film film = createTestFilm();

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getGenres()).isEmpty();
    }

    @Test
    public void testCreateFilmWithGenres() {
        Film film = createTestFilm();
        film.setGenres(Set.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма")));

        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        Set<Genre> loadedGenres = genreStorage.getGenresByFilmId(created.getId());
        assertThat(loadedGenres).extracting("id").containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    public void testFindByIdWithGenres() {
        Film film = createTestFilm();
        film.setGenres(Set.of(new Genre(3L, "Фантастика")));
        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getGenres()).extracting("id").contains(3L);
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Film> found = filmStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    public void testUpdateFilmAndGenres() {
        Film film = createTestFilm();
        film.setGenres(Set.of(new Genre(1L, "Комедия")));
        Film created = filmStorage.create(film);

        created.setName("Updated Title");
        created.setGenres(Set.of(new Genre(4L, "Ужасы")));

        Film updated = filmStorage.update(created);

        Optional<Film> found = filmStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Title");
        assertThat(found.get().getGenres()).extracting("id").containsExactlyInAnyOrder(4L);
    }

    @Test
    public void testFindAll() {
        filmStorage.create(createTestFilm());
        filmStorage.create(createTestFilm("Film Two", "Desc Two"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting("name").containsExactlyInAnyOrder("Test Film", "Film Two");
    }

    @Test
    public void testDelete() {
        Film film = filmStorage.create(createTestFilm());

        filmStorage.delete(film.getId());

        assertThat(filmStorage.findById(film.getId())).isEmpty();
    }

    @Test
    public void testContains() {
        Film film = filmStorage.create(createTestFilm());

        assertThat(filmStorage.contains(film.getId())).isTrue();
        assertThat(filmStorage.contains(999L)).isFalse();
    }

    @Test
    public void testAddAndRemoveLike() {
        Film film = filmStorage.create(createTestFilm());
        User user = createTestUser("liker@test.com", "liker");

        filmStorage.addLike(film.getId(), user.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(), user.getId()
        );
        assertThat(count).isEqualTo(1);

        filmStorage.removeLike(film.getId(), user.getId());

        count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(), user.getId()
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testGetPopular() {
        Film film1 = filmStorage.create(createTestFilm("Film One", "Desc One"));
        Film film2 = filmStorage.create(createTestFilm("Popular Film", "Desc Two"));

        User user1 = createTestUser("u1@test.com", "user1");
        User user2 = createTestUser("u2@test.com", "user2");
        User user3 = createTestUser("u3@test.com", "user3");

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user3.getId());

        Collection<Film> popular = filmStorage.getPopular(2);

        assertThat(popular).hasSize(2);
        assertThat(popular.iterator().next().getName()).isEqualTo("Popular Film");
    }

    @Test
    public void testGetPopularWithLimit() {
        for (int i = 0; i < 5; i++) {
            Film film = createTestFilm("Film " + i, "Desc " + i);
            filmStorage.create(film);
        }

        Collection<Film> popular = filmStorage.getPopular(3);
        assertThat(popular).hasSize(3);
    }

    private Film createTestFilm() {
        return createTestFilm("Test Film", "Test Description");
    }

    private Film createTestFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));
        return film;
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }
}
