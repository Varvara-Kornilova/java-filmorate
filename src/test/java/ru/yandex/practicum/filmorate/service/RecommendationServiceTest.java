package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.recommendation.RecommendationService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class RecommendationServiceTest {

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

    private RecommendationService recommendationService;
    private UserStorage userStorage;
    private FilmStorage filmStorage;
    private LikeDbStorage likeStorage;

    @BeforeEach
    public void setUp() {

        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");

        userStorage = new UserDbStorage(jdbcTemplate, userRowMapper);
        MpaStorage mpaStorage = new MpaDbStorage(jdbcTemplate, mpaRowMapper);
        GenreStorage genreStorage = new GenreDbStorage(jdbcTemplate, genreRowMapper);
        DirectorStorage directorStorage = new DirectorDbStorage(jdbcTemplate, directorRowMapper);
        filmStorage = new FilmDbStorage(jdbcTemplate, filmRowMapper, genreStorage, directorStorage);
        likeStorage = new LikeDbStorage(jdbcTemplate);

        recommendationService = new RecommendationService(likeStorage, filmStorage);
    }

    @Test
    public void getRecommendations_ShouldReturnRecommendedFilms() {

        User user1 = createTestUser("rec1@test.com", "rec1");
        User user2 = createTestUser("rec2@test.com", "rec2");

        Film film1 = createTestFilm("Film 1");
        Film film2 = createTestFilm("Film 2");
        Film film3 = createTestFilm("Film 3");

        likeStorage.addLike(film1.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film 2", "Film 3");
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyForUserWithNoLikes() {

        User user = createTestUser("noLikes@test.com", "noLikes");

        Collection<Film> recommendations = recommendationService.getRecommendations(user.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyForUserWithLikesButNoSimilarUser() {
        User user1 = createTestUser("userA@test.com", "userA");
        User user2 = createTestUser("userB@test.com", "userB");

        Film film1 = createTestFilm("Film A");
        Film film2 = createTestFilm("Film B");

        likeStorage.addLike(film1.getId(), user1.getId());

        likeStorage.addLike(film2.getId(), user2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyWhenAllFilmsAlreadyLiked() {
        User user1 = createTestUser("userC@test.com", "userC");
        User user2 = createTestUser("userD@test.com", "userD");

        Film film1 = createTestFilm("Film X");
        Film film2 = createTestFilm("Film Y");

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void getRecommendations_ShouldReturnMostSimilarUserRecommendations() {
        User user1 = createTestUser("userE@test.com", "userE");
        User user2 = createTestUser("userF@test.com", "userF");
        User user3 = createTestUser("userG@test.com", "userG");

        Film film1 = createTestFilm("Film 1");
        Film film2 = createTestFilm("Film 2");
        Film film3 = createTestFilm("Film 3");
        Film film4 = createTestFilm("Film 4");

        likeStorage.addLike(film1.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        likeStorage.addLike(film1.getId(), user3.getId());
        likeStorage.addLike(film4.getId(), user3.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film 2", "Film 3");
    }

    @Test
    public void getRecommendations_ShouldNotRecommendAlreadyLikedFilms() {
        User user1 = createTestUser("userH@test.com", "userH");
        User user2 = createTestUser("userI@test.com", "userI");

        Film film1 = createTestFilm("Film Alpha");
        Film film2 = createTestFilm("Film Beta");
        Film film3 = createTestFilm("Film Gamma");

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.iterator().next().getName()).isEqualTo("Film Gamma");
    }

    @Test
    public void getRecommendations_ShouldHandleMultipleRecommendations() {
        User user1 = createTestUser("userJ@test.com", "userJ");
        User user2 = createTestUser("userK@test.com", "userK");

        Film film1 = createTestFilm("Film One");
        Film film2 = createTestFilm("Film Two");
        Film film3 = createTestFilm("Film Three");
        Film film4 = createTestFilm("Film Four");
        Film film5 = createTestFilm("Film Five");

        likeStorage.addLike(film1.getId(), user1.getId());

        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film3.getId(), user2.getId());
        likeStorage.addLike(film4.getId(), user2.getId());
        likeStorage.addLike(film5.getId(), user2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(4);
        assertThat(recommendations).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film Two", "Film Three", "Film Four", "Film Five");
    }

    @Test
    public void getRecommendations_ShouldWorkWithEmptyDatabase() {

        User user = createTestUser("empty@test.com", "empty");

        Collection<Film> recommendations = recommendationService.getRecommendations(user.getId());

        assertThat(recommendations).isEmpty();
    }

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description for " + name);
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));
        return filmStorage.create(film);
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
