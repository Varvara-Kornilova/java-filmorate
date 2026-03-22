package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class LikeDbStorageTest extends BaseJdbcTest {

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

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user2.getId());

        Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();

        assertThat(userLikes).hasSize(2);
        assertThat(userLikes.get(user1.getId())).containsExactlyInAnyOrder(film1.getId(), film2.getId());
        assertThat(userLikes.get(user2.getId())).containsExactlyInAnyOrder(film1.getId(), film3.getId());
    }

    @Test
    public void testGetAllUserLikes_AfterRemoveLike() {
        User user = createTestUser("likeRemove@test.com", "likeRemove");
        Film film = createAndSaveTestFilm("Film", "Desc", LocalDate.of(2023, 1, 1));

        filmStorage.addLike(film.getId(), user.getId());

        Map<Long, Set<Long>> userLikes = likeStorage.getAllUserLikes();
        assertThat(userLikes.get(user.getId())).contains(film.getId());

        filmStorage.removeLike(film.getId(), user.getId());

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
