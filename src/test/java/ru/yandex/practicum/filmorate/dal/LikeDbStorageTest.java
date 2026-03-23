package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LikeDbStorageTest extends BaseJdbcTest {

    private LikeDbStorage likeStorage;

    @BeforeEach
    public void setUp() {
        likeStorage = new LikeDbStorage(jdbcTemplate);
    }

    @Test
    public void testGetAllUserLikes_Empty() {
        Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();

        assertThat(userLikes).isEmpty();
    }

    @Test
    public void testGetAllUserLikes_WithData() {

        User user1 = createTestUser("like1@test.com", "like1");
        User user2 = createTestUser("like2@test.com", "like2");

        Film film1 = createAndSaveTestFilm("Film 1", "Desc 1", LocalDate.of(2023, 1, 1));
        Film film2 = createAndSaveTestFilm("Film 2", "Desc 2", LocalDate.of(2023, 2, 1));
        Film film3 = createAndSaveTestFilm("Film 3", "Desc 3", LocalDate.of(2023, 3, 1));

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user1.getId());
        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();

        assertThat(userLikes).hasSize(2);
        assertThat(userLikes.get(user1.getId())).containsExactlyInAnyOrder(film1.getId(), film2.getId());
        assertThat(userLikes.get(user2.getId())).containsExactlyInAnyOrder(film1.getId(), film3.getId());
    }

    @Test
    public void testGetUserLikes() {
        User user = createTestUser("userLikes@test.com", "userLikes");
        Film film1 = createAndSaveTestFilm("Film X", "Desc X", LocalDate.of(2023, 1, 1));
        Film film2 = createAndSaveTestFilm("Film Y", "Desc Y", LocalDate.of(2023, 2, 1));

        likeStorage.addLike(film1.getId(), user.getId());
        likeStorage.addLike(film2.getId(), user.getId());

        Set<Long> userLikes = likeStorage.getUserLikes(user.getId());

        assertThat(userLikes).hasSize(2);
        assertThat(userLikes).containsExactlyInAnyOrder(film1.getId(), film2.getId());
    }

    @Test
    public void testHasLike() {
        User user = createTestUser("hasLike@test.com", "hasLike");
        Film film = createAndSaveTestFilm("Film Z", "Desc Z", LocalDate.of(2023, 1, 1));

        assertThat(likeStorage.hasLike(film.getId(), user.getId())).isFalse();

        likeStorage.addLike(film.getId(), user.getId());

        assertThat(likeStorage.hasLike(film.getId(), user.getId())).isTrue();
    }

    @Test
    public void testAddAndRemoveLike() {
        User user = createTestUser("addRemove@test.com", "addRemove");
        Film film = createAndSaveTestFilm("Film R", "Desc R", LocalDate.of(2023, 1, 1));

        likeStorage.addLike(film.getId(), user.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(),
                user.getId()
        );
        assertThat(count).isEqualTo(1);

        likeStorage.removeLike(film.getId(), user.getId());

        count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                film.getId(),
                user.getId()
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testGetAllUserLikes_AfterRemoveLike() {
        User user = createTestUser("removeAfter@test.com", "removeAfter");
        Film film = createAndSaveTestFilm("Film W", "Desc W", LocalDate.of(2023, 1, 1));

        likeStorage.addLike(film.getId(), user.getId());

        Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();
        assertThat(userLikes.get(user.getId())).contains(film.getId());

        likeStorage.removeLike(film.getId(), user.getId());

        userLikes = likeStorage.getAllUserLikes();
        assertThat(userLikes.get(user.getId())).isNullOrEmpty();
    }

    private Film createAndSaveTestFilm(String name, String description, LocalDate releaseDate) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
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
