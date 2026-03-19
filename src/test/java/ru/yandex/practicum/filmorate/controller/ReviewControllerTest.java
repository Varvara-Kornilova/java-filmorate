package ru.yandex.practicum.filmorate.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.review.ReviewService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase
public class ReviewControllerTest {

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private UserRowMapper userRowMapper;
    @Autowired private FilmRowMapper filmRowMapper;
    @Autowired private ReviewRowMapper reviewRowMapper;
    @Autowired private GenreStorage genreStorage;
    @Autowired private DirectorStorage directorStorage;

    private ReviewController controller;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;

    @BeforeEach
    void init() {
        clearTestData();
        userStorage = new UserDbStorage(jdbcTemplate, userRowMapper);
        filmStorage = new FilmDbStorage(jdbcTemplate, filmRowMapper, genreStorage, directorStorage);
        ReviewDbStorage reviewStorage = new ReviewDbStorage(jdbcTemplate, reviewRowMapper);

        ReviewService reviewService = new ReviewService(reviewStorage, userStorage, filmStorage);
        controller = new ReviewController(reviewService);
    }


    private User createTestUser() {
        long timestamp = System.nanoTime();
        User user = new User();
        user.setEmail("test" + timestamp + "@mail.ru");
        user.setLogin("login" + timestamp);
        user.setName("Name" + timestamp);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    private Film createTestFilm() {
        long timestamp = System.nanoTime();
        Film film = new Film();
        film.setName("Film " + timestamp);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        film.setMpa(Mpa.of(1L, "G"));
        return filmStorage.create(film);
    }

    private Review buildReview(Long userId, Long filmId) {
        return Review.builder()
                .content("Тестовый отзыв")
                .isPositive(true)
                .userId(userId)
                .filmId(filmId)
                .build();
    }

    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM review_likes");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
    }

    @Test
    void create_ReturnSavedReview_whenDataIsValid() {
        User user = createTestUser();
        Film film = createTestFilm();
        Review review = buildReview(user.getId(), film.getId());

        Review savedReview = controller.create(review);

        assertNotNull(savedReview.getReviewId());
        assertEquals(review.getContent(), savedReview.getContent());
    }

    @Test
    void update_UpdateReviewFields_whenDataIsValid() {
        User user = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(user.getId(), film.getId()));

        Review updateRequest = Review.builder()
                .reviewId(review.getReviewId())
                .content("Какой-то отзыв")
                .isPositive(false)
                .userId(user.getId())
                .filmId(film.getId())
                .build();

        Review updated = controller.update(updateRequest);

        assertEquals("Какой-то отзыв", updated.getContent());
        assertFalse(updated.getIsPositive());
        assertEquals(review.getReviewId(), updated.getReviewId());
    }

    @Test
    void delete_RemoveReview_whenReviewExists() {
        User user = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(user.getId(), film.getId()));

        controller.delete(review.getReviewId());

        assertThrows(NotFoundException.class, () -> controller.findById(review.getReviewId()));
    }

    @Test
    void findAll_ReturnReviewsSortedByUsefulRating_whenCalledWithoutFilmId() {
        User user = createTestUser();
        Film film = createTestFilm();

        controller.create(buildReview(user.getId(), film.getId()));
        Review secondReview = controller.create(buildReview(user.getId(), film.getId()));

        controller.addLike(secondReview.getReviewId(), user.getId());

        List<Review> reviews = (List<Review>) controller.findAll(null, 10);

        assertEquals(secondReview.getReviewId(), reviews.getFirst().getReviewId());
    }

    @Test
    void addLike_IncreaseUsefulRating() {
        User liker = createTestUser();
        User author = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(author.getId(), film.getId()));

        controller.addLike(review.getReviewId(), liker.getId());

        assertEquals(1, controller.findById(review.getReviewId()).getUseful());
    }

    @Test
    void addDislike_DecreaseUsefulRating() {
        User disliker = createTestUser();
        User author = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(author.getId(), film.getId()));

        controller.addDislike(review.getReviewId(), disliker.getId());

        assertEquals(-1, controller.findById(review.getReviewId()).getUseful());
    }
}