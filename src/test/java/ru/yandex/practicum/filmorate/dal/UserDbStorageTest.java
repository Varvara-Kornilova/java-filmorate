package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDbStorageTest extends BaseJdbcTest {

    @Autowired
    private UserRowMapper userRowMapper;
    @Autowired
    private FilmRowMapper filmRowMapper;
    @Autowired
    private GenreStorage genreStorage;
    @Autowired
    private DirectorStorage directorStorage;

    private UserStorage userStorage;

    @BeforeEach
    public void setUp() {
        cleanUp();

        userStorage = new UserDbStorage(jdbcTemplate, userRowMapper, filmRowMapper, genreStorage, directorStorage);
    }

    @Override
    protected void cleanUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
    }

    @Test
    public void testCreateAndFindById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);
        assertThat(created.getId()).isNotNull();

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getLogin()).isEqualTo("testuser");
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testUpdate() {
        User user = createUser("old@example.com", "oldlogin", "Old Name");
        user.setName("Updated Name");
        user.setEmail("new@example.com");

        User updated = userStorage.update(user);
        Optional<User> found = userStorage.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
        assertThat(found.get().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    public void testFindAll() {
        createUser("u1@test.com", "user1", "User One");
        createUser("u2@test.com", "user2", "User Two");

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting("login").containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    public void testDelete() {
        User created = createUser("todelete@example.com", "todelete", "ToDelete");
        userStorage.delete(created.getId());
        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    public void testContains() {
        User created = createUser("exists@example.com", "exists", "Exists");
        assertThat(userStorage.contains(created.getId())).isTrue();
        assertThat(userStorage.contains(999L)).isFalse();
    }

    @Test
    public void testGetRecommendations_ShouldReturnRecommendedFilms() {
        User targetUser = createUser("target@test.com", "target", "Target User");
        User similarUser = createUser("similar@test.com", "similar", "Similar User");
        User otherUser = createUser("other@test.com", "other", "Other User");

        Film film1 = createFilm("Common Film 1");
        Film film2 = createFilm("Common Film 2");
        Film film3 = createFilm("Recommended Film 1");
        Film film4 = createFilm("Recommended Film 2");
        Film film5 = createFilm("Not Recommended Film");

        addLike(film1.getId(), targetUser.getId());
        addLike(film2.getId(), targetUser.getId());

        addLike(film1.getId(), similarUser.getId());
        addLike(film2.getId(), similarUser.getId());
        addLike(film3.getId(), similarUser.getId());
        addLike(film4.getId(), similarUser.getId());

        addLike(film1.getId(), otherUser.getId());
        addLike(film5.getId(), otherUser.getId());

        Collection<Film> recommendations = userStorage.getRecommendations(targetUser.getId());

        assertThat(recommendations).hasSize(2);
        assertThat(recommendations).extracting(Film::getName)
                .containsExactlyInAnyOrder("Recommended Film 1", "Recommended Film 2");
    }

    @Test
    public void testGetRecommendations_ShouldReturnEmpty_WhenNoSimilarUser() {
        User targetUser = createUser("alone@test.com", "alone", "Alone User");
        User anotherUser = createUser("another@test.com", "another", "Another User");

        Film film1 = createFilm("Film A");
        Film film2 = createFilm("Film B");

        addLike(film1.getId(), targetUser.getId());
        addLike(film2.getId(), anotherUser.getId());

        Collection<Film> recommendations = userStorage.getRecommendations(targetUser.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void testGetRecommendations_ShouldSortByLikesCount() {
        User targetUser = createUser("target2@test.com", "target2", "Target 2");
        User similarUser = createUser("similar2@test.com", "similar2", "Similar 2");

        User extra1 = createUser("extra1@test.com", "extra1", "Extra 1");
        User extra2 = createUser("extra2@test.com", "extra2", "Extra 2");
        User extra3 = createUser("extra3@test.com", "extra3", "Extra 3");

        Film commonFilm = createFilm("Common Film");
        Film popularRecommended = createFilm("Popular Recommended");
        Film lessPopularRecommended = createFilm("Less Popular Recommended");

        addLike(commonFilm.getId(), targetUser.getId());

        addLike(commonFilm.getId(), similarUser.getId());
        addLike(popularRecommended.getId(), similarUser.getId());
        addLike(lessPopularRecommended.getId(), similarUser.getId());

        addLike(popularRecommended.getId(), extra1.getId());
        addLike(popularRecommended.getId(), extra2.getId());
        addLike(popularRecommended.getId(), extra3.getId());

        addLike(lessPopularRecommended.getId(), extra1.getId());

        Collection<Film> recommendations = userStorage.getRecommendations(targetUser.getId());

        List<Film> filmList = new ArrayList<>(recommendations);
        assertThat(filmList).hasSize(2);
        assertThat(filmList.get(0).getName()).isEqualTo("Popular Recommended");
        assertThat(filmList.get(1).getName()).isEqualTo("Less Popular Recommended");
    }

    @Test
    public void testGetRecommendations_ShouldReturnEmpty_WhenNoFilms() {
        User targetUser = createUser("noFilms@test.com", "nofilms", "No Films");

        Collection<Film> recommendations = userStorage.getRecommendations(targetUser.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    public void testGetRecommendations_ShouldReturnEmpty_WhenUserHasNoLikes() {
        User targetUser = createUser("noLikes@test.com", "nolikes", "No Likes");
        User similarUser = createUser("similar3@test.com", "similar3", "Similar 3");

        Film film1 = createFilm("Film 1");
        Film film2 = createFilm("Film 2");

        addLike(film1.getId(), similarUser.getId());
        addLike(film2.getId(), similarUser.getId());

        Collection<Film> recommendations = userStorage.getRecommendations(targetUser.getId());

        assertThat(recommendations).isEmpty();
    }

    private User createUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description for " + name);
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                            film.getDuration(), film.getMpa().getId());

        Long id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
        film.setId(id);

        return film;
    }

    private void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }
}
