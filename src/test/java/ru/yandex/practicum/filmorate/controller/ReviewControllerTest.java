package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.EventDbStorage;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.ReviewDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.review.ReviewService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
public class ReviewControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserRowMapper userRowMapper;
    @Autowired
    private FilmRowMapper filmRowMapper;
    @Autowired
    private ReviewRowMapper reviewRowMapper;
    @Autowired
    private EventRowMapper eventRowMapper;
    @Autowired
    private GenreStorage genreStorage;
    @Autowired
    private DirectorStorage directorStorage;

    private ReviewController controller;
    private UserDbStorage userStorage;
    private FilmDbStorage filmStorage;

    @BeforeEach
    public void init() {
        // очищаем тестовые данные
        clearTestData();

        userStorage = new UserDbStorage(jdbcTemplate, userRowMapper, filmRowMapper, genreStorage, directorStorage);
        filmStorage = new FilmDbStorage(jdbcTemplate, filmRowMapper, genreStorage, directorStorage);
        ReviewDbStorage reviewStorage = new ReviewDbStorage(jdbcTemplate, reviewRowMapper);
        EventDbStorage eventStorage = new EventDbStorage(jdbcTemplate, eventRowMapper);
        EventService eventService = new EventService(eventStorage, userStorage);

        ReviewService reviewService = new ReviewService(reviewStorage, userStorage, filmStorage, eventService);
        controller = new ReviewController(reviewService);
    }

    // создаем тестового пользователя
    private User createTestUser() {
        long timestamp = System.nanoTime();
        User user = new User();
        user.setEmail("test" + timestamp + "@mail.ru");
        user.setLogin("login" + timestamp);
        user.setName("Name" + timestamp);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    // создаем тестовый фильм
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

    // создаем тестовый отзыв
    private Review buildReview(Long userId, Long filmId) {
        return Review.builder()
                .content("Тестовый отзыв")
                .isPositive(true)
                .userId(userId)
                .filmId(filmId)
                .build();
    }

    // очищаем таблицы перед тестами
    private void clearTestData() {
        jdbcTemplate.update("DELETE FROM events");
        jdbcTemplate.update("DELETE FROM review_likes");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM films");

        jdbcTemplate.update("ALTER TABLE events ALTER COLUMN event_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
    }

    @Test
    public void create_ReturnSavedReview_whenDataIsValid() {
        // создаем пользователя, фильм и отзыв
        User user = createTestUser();
        Film film = createTestFilm();
        Review review = buildReview(user.getId(), film.getId());

        // сохраняем отзыв
        Review savedReview = controller.create(review);

        // проверяем, что отзыв сохранился
        assertNotNull(savedReview.getReviewId());
        assertEquals(review.getContent(), savedReview.getContent());
    }

    @Test
    public void update_UpdateReviewFields_whenDataIsValid() {
        // создаем пользователя, фильм и отзыв
        User user = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(user.getId(), film.getId()));

        // меняем данные отзыва
        Review updateRequest = Review.builder()
                .reviewId(review.getReviewId())
                .content("Какой-то отзыв")
                .isPositive(false)
                .userId(user.getId())
                .filmId(film.getId())
                .build();

        // обновляем отзыв
        Review updated = controller.update(updateRequest);

        // проверяем, что отзыв обновился
        assertEquals("Какой-то отзыв", updated.getContent());
        assertFalse(updated.getIsPositive());
        assertEquals(review.getReviewId(), updated.getReviewId());
    }

    @Test
    public void delete_RemoveReview_whenReviewExists() {
        // создаем пользователя, фильм и отзыв
        User user = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(user.getId(), film.getId()));

        // удаляем отзыв
        controller.delete(review.getReviewId());

        // проверяем, что отзыв удален
        assertThrows(NotFoundException.class, () -> controller.findById(review.getReviewId()));
    }

    @Test
    public void findAll_ReturnReviewsSortedByUsefulRating_whenCalledWithoutFilmId() {
        // создаем пользователя и фильм
        User user = createTestUser();
        Film film = createTestFilm();

        // создаем два отзыва
        controller.create(buildReview(user.getId(), film.getId()));
        Review secondReview = controller.create(buildReview(user.getId(), film.getId()));

        // ставим лайк второму отзыву
        controller.addLike(secondReview.getReviewId(), user.getId());

        // получаем список отзывов
        List<Review> reviews = (List<Review>) controller.findAll(null, 10);

        // проверяем, что самый полезный отзыв идет первым
        assertEquals(secondReview.getReviewId(), reviews.getFirst().getReviewId());
    }

    @Test
    public void addLike_IncreaseUsefulRating() {
        // создаем пользователей, фильм и отзыв
        User liker = createTestUser();
        User author = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(author.getId(), film.getId()));

        // ставим лайк отзыву
        controller.addLike(review.getReviewId(), liker.getId());

        // проверяем, что полезность увеличилась
        assertEquals(1, controller.findById(review.getReviewId()).getUseful());
    }

    @Test
    public void addDislike_DecreaseUsefulRating() {
        // создаем пользователей, фильм и отзыв
        User disliker = createTestUser();
        User author = createTestUser();
        Film film = createTestFilm();
        Review review = controller.create(buildReview(author.getId(), film.getId()));

        // ставим дизлайк отзыву
        controller.addDislike(review.getReviewId(), disliker.getId());

        // проверяем, что полезность уменьшилась
        assertEquals(-1, controller.findById(review.getReviewId()).getUseful());
    }
}
