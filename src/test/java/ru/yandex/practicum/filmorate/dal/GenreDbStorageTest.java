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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        GenreDbStorage.class,
        FilmDbStorage.class,
        GenreRowMapper.class,
        FilmRowMapper.class
})
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void cleanUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
    }

    @Test
    public void testFindAll() {
        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).extracting("id").containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(genres).extracting("name").containsExactlyInAnyOrder(
                "Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    public void testFindById() {
        Optional<Genre> found = genreStorage.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Комедия");
    }

    @Test
    public void testSetAndGetGenresForFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        Set<Long> genreIds = Set.of(1L, 3L);
        genreStorage.setGenres(created.getId(), genreIds);

        Set<Genre> found = genreStorage.getGenresByFilmId(created.getId());
        assertThat(found).extracting("id").containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    public void testUpdateFilmGenres() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        genreStorage.setGenres(created.getId(), Set.of(1L, 2L));
        genreStorage.updateFilmGenres(created.getId());
        genreStorage.setGenres(created.getId(), Set.of(4L));

        Set<Genre> found = genreStorage.getGenresByFilmId(created.getId());
        assertThat(found).extracting("id").containsExactlyInAnyOrder(4L);
        assertThat(found).extracting("id").doesNotContain(1L, 2L);
    }

    @Test
    public void testSetGenresEmpty() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        genreStorage.setGenres(created.getId(), Set.of());

        Set<Genre> found = genreStorage.getGenresByFilmId(created.getId());
        assertThat(found).isEmpty();
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));
        return film;
    }
}
