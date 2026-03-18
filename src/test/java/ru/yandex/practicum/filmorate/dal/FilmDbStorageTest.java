package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class FilmDbStorageTest extends BaseJdbcTest {

    @BeforeEach
    public void additionalCleanUp() {
        super.cleanUp();
        jdbcTemplate.update("DELETE FROM film_directors");
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
    public void testFindByIdNotFound() {
        Optional<Film> found = filmStorage.findById(999L);
        assertThat(found).isEmpty();
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
                Integer.class, film.getId(), user.getId());
        assertThat(count).isEqualTo(1);

        filmStorage.removeLike(film.getId(), user.getId());

        count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class, film.getId(), user.getId());
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
            filmStorage.create(createTestFilm("Film " + i, "Desc " + i));
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
