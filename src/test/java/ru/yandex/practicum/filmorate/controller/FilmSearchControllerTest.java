package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
public class FilmSearchControllerTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRowMapper userRowMapper;
    @Autowired private FilmRowMapper filmRowMapper;
    @Autowired private GenreRowMapper genreRowMapper;
    @Autowired private DirectorRowMapper directorRowMapper;
    @Autowired private MpaRowMapper mpaRowMapper;

    private FilmController filmController;
    private DirectorController directorController;
    private UserStorage userStorage;
    private FilmStorage filmStorage;
    private GenreStorage genreStorage;
    private DirectorStorage directorStorage;
    private MpaStorage mpaStorage;

    @BeforeEach
    public void init() {
        clearTestData();

        userStorage = new UserDbStorage(jdbcTemplate, userRowMapper);
        mpaStorage = new MpaDbStorage(jdbcTemplate, mpaRowMapper);
        genreStorage = new GenreDbStorage(jdbcTemplate, genreRowMapper);
        directorStorage = new DirectorDbStorage(jdbcTemplate, directorRowMapper);
        filmStorage = new FilmDbStorage(jdbcTemplate, filmRowMapper, genreStorage, directorStorage);

        MpaService mpaService = new MpaService(mpaStorage);
        GenreService genreService = new GenreService(genreStorage);
        DirectorService directorService = new DirectorService(directorStorage);

        FilmService filmService = new FilmService(
                filmStorage, userStorage, mpaService, genreService,
                directorService, directorStorage, genreStorage
        );

        filmController = new FilmController(filmService);
        directorController = new DirectorController(directorService);
    }

    @Test
    public void search_ByDirector_ReturnsMatchingFilms() {
        Director director = createTestDirector("DirectorSearchName");
        Film film = createTestFilmWithDirector("Film By Director", director.getId());

        Collection<Film> results = filmController.searchFilms("DirectorSearch", "director");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(film.getName(), results.iterator().next().getName());
    }

    @Test
    public void search_ByTitleAndDirector_ReturnsCombinedResults() {
        Film film1 = createTestFilm("UniqueSearchWord2024");

        Director director = createTestDirector("UniqueSearchWordDir");
        Film film2 = createTestFilmWithDirector("Another Film", director.getId());

        Collection<Film> results = filmController.searchFilms("UniqueSearchWord", "title,director");

        assertEquals(2, results.size());
    }

    @Test
    public void search_ByTitle_ReturnsMatchingFilms() {
        Film film = createTestFilm("SearchTestTitle2024");

        Collection<Film> results = filmController.searchFilms("SearchTest", "title");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(film.getName(), results.iterator().next().getName());
    }

    @Test
    public void search_EmptyResult_ReturnsEmptyList() {
        createTestFilm("CompletelyDifferentName");

        Collection<Film> results = filmController.searchFilms("NonExistentWord12345", "title");

        assertTrue(results.isEmpty());
    }

    @Test
    public void search_InvalidByParameter_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            filmController.searchFilms("test", "invalid");
        });
    }


    private Film createTestFilmWithDirector(String name, Long directorId) {
        long timestamp = System.nanoTime();
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description " + timestamp);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(Mpa.of(1L, "G"));
        film.setGenres(new HashSet<>());

        if (directorId != null) {
            Director director = new Director();
            director.setId(directorId);
            film.setDirectors(new HashSet<>(List.of(director)));
        } else {
            film.setDirectors(new HashSet<>());
        }

        return filmController.registerFilm(film);
    }

    private Film createTestFilm(String name) {
        return createTestFilmWithDirector(name, null);
    }

    private Director createTestDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorController.createDirector(director);
    }

    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM review_likes");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM directors");

        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 1");
    }
}
