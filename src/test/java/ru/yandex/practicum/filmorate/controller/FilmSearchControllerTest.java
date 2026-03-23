package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.service.recommendation.RecommendationService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
public class FilmSearchControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserRowMapper userRowMapper;
    @Autowired
    private FilmRowMapper filmRowMapper;
    @Autowired
    private GenreRowMapper genreRowMapper;
    @Autowired
    private DirectorRowMapper directorRowMapper;
    @Autowired
    private MpaRowMapper mpaRowMapper;
    @Autowired
    private EventRowMapper eventRowMapper;

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
        EventDbStorage eventStorage = new EventDbStorage(jdbcTemplate, eventRowMapper);
        EventService eventService = new EventService(eventStorage, userStorage);
        LikeDbStorage likeStorage = new LikeDbStorage(jdbcTemplate);
        RecommendationService recommendationService = new RecommendationService(likeStorage, filmStorage);

        FilmService filmService = new FilmService(
                filmStorage,
                userStorage,
                mpaService,
                genreService,
                directorService,
                directorStorage,
                genreStorage,
                eventService,
                recommendationService,
                likeStorage
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
        createTestFilm("UniqueSearchWord2024");

        Director director = createTestDirector("UniqueSearchWordDir");
        createTestFilmWithDirector("Another Film", director.getId());

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
        assertThrows(ValidationException.class, () ->
                filmController.searchFilms("test", "invalid"));
    }

    @Test
    public void getCommonFilms_ShouldReturnCommonFilms() {
        User user1 = createTestUser("common1@test.com", "common1");
        User user2 = createTestUser("common2@test.com", "common2");

        Film film1 = createTestFilm("Common Film 1");
        Film film2 = createTestFilm("Common Film 2");
        Film film3 = createTestFilm("Not Common Film");

        filmController.applyLike(film1.getId(), user1.getId());
        filmController.applyLike(film1.getId(), user2.getId());
        filmController.applyLike(film2.getId(), user1.getId());
        filmController.applyLike(film2.getId(), user2.getId());
        filmController.applyLike(film3.getId(), user1.getId());

        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        assertFalse(commonFilms.isEmpty());
        assertEquals(2, commonFilms.size());
        assertThat(commonFilms).extracting(Film::getName)
                .containsExactlyInAnyOrder("Common Film 1", "Common Film 2");
    }

    @Test
    public void getCommonFilms_ShouldReturnEmptyList_WhenNoCommonFilms() {
        User user1 = createTestUser("noCommon1@test.com", "noCommon1");
        User user2 = createTestUser("noCommon2@test.com", "noCommon2");

        Film film1 = createTestFilm("Film A");
        Film film2 = createTestFilm("Film B");

        filmController.applyLike(film1.getId(), user1.getId());
        filmController.applyLike(film2.getId(), user2.getId());

        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        assertTrue(commonFilms.isEmpty());
    }

    @Test
    public void getCommonFilms_ShouldThrowException_WhenUserNotFound() {
        User user = createTestUser("existing@test.com", "existing");

        assertThrows(NotFoundException.class, () -> {
            filmController.getCommonFilms(999L, user.getId());
        });
    }

    @Test
    public void getCommonFilms_ShouldThrowException_WhenFriendNotFound() {
        User user = createTestUser("existing2@test.com", "existing2");

        assertThrows(NotFoundException.class, () -> {
            filmController.getCommonFilms(user.getId(), 999L);
        });
    }

    @Test
    public void getRecommendations_ShouldReturnRecommendations() {

        User user1 = createTestUser("recTest1@test.com", "recTest1");
        User user2 = createTestUser("recTest2@test.com", "recTest2");

        Film film1 = createTestFilm("Rec Film 1");
        Film film2 = createTestFilm("Rec Film 2");
        Film film3 = createTestFilm("Rec Film 3");

        filmController.applyLike(film1.getId(), user1.getId());

        filmController.applyLike(film1.getId(), user2.getId());
        filmController.applyLike(film2.getId(), user2.getId());
        filmController.applyLike(film3.getId(), user2.getId());

        Collection<Film> recommendations = filmController.getRecommendations(user1.getId());

        assertFalse(recommendations.isEmpty());
        assertEquals(2, recommendations.size());
        assertThat(recommendations).extracting(Film::getName)
                .containsExactlyInAnyOrder("Rec Film 2", "Rec Film 3");
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyList_WhenNoRecommendations() {
        User user = createTestUser("emptyRecTest@test.com", "emptyRecTest");

        Collection<Film> recommendations = filmController.getRecommendations(user.getId());

        assertTrue(recommendations.isEmpty());
    }

    @Test
    public void getRecommendations_ShouldThrowException_WhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> {
            filmController.getRecommendations(999L);
        });
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyList_WhenUserHasNoLikes() {
        User user = createTestUser("noLikesUser@test.com", "noLikesUser");

        Collection<Film> recommendations = filmController.getRecommendations(user.getId());

        assertTrue(recommendations.isEmpty());
    }

    @Test
    public void getRecommendations_ShouldReturnOnlyUnwatchedFilms() {

        User user1 = createTestUser("recUserA@test.com", "recUserA");
        User user2 = createTestUser("recUserB@test.com", "recUserB");

        Film film1 = createTestFilm("Film A");
        Film film2 = createTestFilm("Film B");
        Film film3 = createTestFilm("Film C");

        filmController.applyLike(film1.getId(), user1.getId());
        filmController.applyLike(film2.getId(), user1.getId());

        filmController.applyLike(film1.getId(), user2.getId());
        filmController.applyLike(film2.getId(), user2.getId());
        filmController.applyLike(film3.getId(), user2.getId());

        Collection<Film> recommendations = filmController.getRecommendations(user1.getId());

        assertEquals(1, recommendations.size());
        assertEquals(film3.getName(), recommendations.iterator().next().getName());
    }

    @Test
    public void getRecommendations_ShouldReturnRecommendationsFromMostSimilarUser() {

        User user1 = createTestUser("userX@test.com", "userX");
        User user2 = createTestUser("userY@test.com", "userY");
        User user3 = createTestUser("userZ@test.com", "userZ");

        Film film1 = createTestFilm("Film Alpha");
        Film film2 = createTestFilm("Film Beta");
        Film film3 = createTestFilm("Film Gamma");
        Film film4 = createTestFilm("Film Delta");

        filmController.applyLike(film1.getId(), user1.getId());

        filmController.applyLike(film1.getId(), user2.getId());
        filmController.applyLike(film2.getId(), user2.getId());
        filmController.applyLike(film3.getId(), user2.getId());

        filmController.applyLike(film1.getId(), user3.getId());
        filmController.applyLike(film4.getId(), user3.getId());

        Collection<Film> recommendations = filmController.getRecommendations(user1.getId());

        assertFalse(recommendations.isEmpty());
        assertEquals(2, recommendations.size());
        assertThat(recommendations).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film Beta", "Film Gamma");
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

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM review_likes");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM directors");

        jdbcTemplate.update("ALTER TABLE events ALTER COLUMN event_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 1");
    }
}
