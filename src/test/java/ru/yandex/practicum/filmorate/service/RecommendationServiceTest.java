package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dal.BaseJdbcTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.recommendation.RecommendationService;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
public class RecommendationServiceTest extends BaseJdbcTest {

    @Autowired
    private RecommendationService recommendationService;

    private User user1;
    private User user2;
    private User user3;
    private Film film1;
    private Film film2;
    private Film film3;
    private Film film4;

    @BeforeEach
    public void setUp() {

        user1 = createTestUser("rec1@test.com", "rec1");
        user2 = createTestUser("rec2@test.com", "rec2");
        user3 = createTestUser("rec3@test.com", "rec3");

        film1 = createTestFilm("Film 1");
        film2 = createTestFilm("Film 2");
        film3 = createTestFilm("Film 3");
        film4 = createTestFilm("Film 4");
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyForUserWithNoLikes() {
        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void getRecommendations_ShouldReturnRecommendedFilms() {

        addLike(user1.getId(), film1.getId());
        addLike(user1.getId(), film2.getId());

        addLike(user2.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());
        addLike(user2.getId(), film3.getId());

        addLike(user3.getId(), film1.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.iterator().next().getId()).isEqualTo(film3.getId());
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyWhenNoSimilarUser() {
        addLike(user1.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void getRecommendations_ShouldReturnEmptyWhenNoUnwatchedFilms() {
        addLike(user1.getId(), film1.getId());
        addLike(user1.getId(), film2.getId());

        addLike(user2.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void getRecommendations_ShouldReturnFilmsFromMostSimilarUser() {

        addLike(user1.getId(), film1.getId());

        addLike(user2.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());
        addLike(user2.getId(), film3.getId());

        addLike(user3.getId(), film1.getId());
        addLike(user3.getId(), film4.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations).extracting(Film::getId)
                .containsExactlyInAnyOrder(film2.getId(), film3.getId());
    }

    @Test
    public void getRecommendations_ShouldWorkWithMultipleSimilarUsers() {
        addLike(user1.getId(), film1.getId());

        addLike(user2.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());

        addLike(user3.getId(), film1.getId());
        addLike(user3.getId(), film3.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations).extracting(Film::getId)
                .containsExactlyInAnyOrder(film2.getId(), film3.getId());
    }

    @Test
    public void getRecommendations_ShouldNotRecommendAlreadyLikedFilms() {
        addLike(user1.getId(), film1.getId());
        addLike(user1.getId(), film2.getId());

        addLike(user2.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());
        addLike(user2.getId(), film3.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(1);
        assertThat(recommendations.iterator().next().getId()).isEqualTo(film3.getId());
    }

    @Test
    public void getRecommendations_ShouldHandleUserWithAllPossibleRecommendations() {
        addLike(user1.getId(), film1.getId());

        addLike(user2.getId(), film1.getId());
        addLike(user2.getId(), film2.getId());
        addLike(user2.getId(), film3.getId());
        addLike(user2.getId(), film4.getId());

        Collection<Film> recommendations = recommendationService.getRecommendations(user1.getId());

        assertThat(recommendations).hasSize(3);
        assertThat(recommendations).extracting(Film::getId)
                .containsExactlyInAnyOrder(film2.getId(), film3.getId(), film4.getId());
    }

    private void addLike(Long userId, Long filmId) {
        filmStorage.addLike(filmId, userId);
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
