package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FilmDbStorageTest extends BaseJdbcTest {

    private LikeDbStorage likeStorage;

    @BeforeEach
    public void setUp() {
        likeStorage = new LikeDbStorage(jdbcTemplate);
    }

    @Test
    public void testCreateFilmWithoutGenres() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getGenres()).isEmpty();
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<Film> found = filmStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindAll() {
        filmStorage.create(createTestFilm());
        filmStorage.create(createTestFilm("Film Two", "Desc Two", LocalDate.of(2023, 1, 1)));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("Test Film", "Film Two");
    }

    @Test
    public void testDelete() {
        Film film = filmStorage.create(createTestFilm());

        filmStorage.delete(film.getId());

        assertThat(filmStorage.findById(film.getId())).isEmpty();
    }

    @Test
    public void testContains() {
        Film film = filmStorage.create(createTestFilm());

        assertThat(filmStorage.contains(film.getId())).isTrue();
        assertThat(filmStorage.contains(999L)).isFalse();
    }

    @Test
    public void testAddAndRemoveLike() {
        Film film = filmStorage.create(createTestFilm());
        User user = createTestUser("liker@test.com", "liker");

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
    public void testGetPopularWithoutFilters() {
        Film film1 = filmStorage.create(createTestFilm("Film One", "Desc One", LocalDate.of(2022, 1, 1)));
        Film film2 = filmStorage.create(createTestFilm("Popular Film", "Desc Two", LocalDate.of(2023, 1, 1)));

        User user1 = createTestUser("u1@test.com", "user1");
        User user2 = createTestUser("u2@test.com", "user2");
        User user3 = createTestUser("u3@test.com", "user3");

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user3.getId());

        Collection<Film> popular = filmStorage.getPopular(2, null, null);

        assertThat(popular).hasSize(2);
        assertThat(popular.iterator().next().getName()).isEqualTo("Popular Film");
    }

    @Test
    public void testGetPopularWithLimit() {
        for (int i = 0; i < 5; i++) {
            filmStorage.create(createTestFilm("Film " + i, "Desc " + i, LocalDate.of(2024, 1, 1)));
        }

        Collection<Film> popular = filmStorage.getPopular(3, null, null);

        assertThat(popular).hasSize(3);
    }

    @Test
    public void testGetPopularFilteredByGenre() {
        Film comedyFilm = filmStorage.create(createTestFilm("Comedy Film", "Funny", LocalDate.of(2024, 1, 1)));
        Film dramaFilm = filmStorage.create(createTestFilm("Drama Film", "Sad", LocalDate.of(2024, 1, 1)));

        genreStorage.setGenres(comedyFilm.getId(), Set.of(1L));
        genreStorage.setGenres(dramaFilm.getId(), Set.of(2L));

        User user1 = createTestUser("genre1@test.com", "genre1");
        User user2 = createTestUser("genre2@test.com", "genre2");

        likeStorage.addLike(comedyFilm.getId(), user1.getId());
        likeStorage.addLike(dramaFilm.getId(), user1.getId());
        likeStorage.addLike(dramaFilm.getId(), user2.getId());

        Collection<Film> popular = filmStorage.getPopular(10, 1L, null);

        assertThat(popular).hasSize(1);
        assertThat(popular.iterator().next().getName()).isEqualTo("Comedy Film");
    }

    @Test
    public void testGetPopularFilteredByYear() {
        Film oldFilm = filmStorage.create(createTestFilm("Old Film", "Old", LocalDate.of(2023, 1, 1)));
        Film newFilm = filmStorage.create(createTestFilm("New Film", "New", LocalDate.of(2024, 1, 1)));

        User user1 = createTestUser("year1@test.com", "year1");
        User user2 = createTestUser("year2@test.com", "year2");

        likeStorage.addLike(oldFilm.getId(), user1.getId());
        likeStorage.addLike(newFilm.getId(), user1.getId());
        likeStorage.addLike(newFilm.getId(), user2.getId());

        Collection<Film> popular = filmStorage.getPopular(10, null, 2023);

        assertThat(popular).hasSize(1);
        assertThat(popular.iterator().next().getName()).isEqualTo("Old Film");
    }

    @Test
    public void testGetPopularFilteredByGenreAndYear() {
        Film neededFilm = filmStorage.create(createTestFilm("Needed Film", "Target", LocalDate.of(2024, 5, 1)));
        Film sameGenreOtherYear = filmStorage.create(createTestFilm("Same Genre Other Year", "Other", LocalDate.of(2023, 5, 1)));
        Film sameYearOtherGenre = filmStorage.create(createTestFilm("Same Year Other Genre", "Other", LocalDate.of(2024, 6, 1)));

        genreStorage.setGenres(neededFilm.getId(), Set.of(1L));
        genreStorage.setGenres(sameGenreOtherYear.getId(), Set.of(1L));
        genreStorage.setGenres(sameYearOtherGenre.getId(), Set.of(2L));

        User user1 = createTestUser("both1@test.com", "both1");
        User user2 = createTestUser("both2@test.com", "both2");
        User user3 = createTestUser("both3@test.com", "both3");

        likeStorage.addLike(neededFilm.getId(), user1.getId());
        likeStorage.addLike(neededFilm.getId(), user2.getId());
        likeStorage.addLike(sameGenreOtherYear.getId(), user3.getId());
        likeStorage.addLike(sameYearOtherGenre.getId(), user3.getId());

        Collection<Film> popular = filmStorage.getPopular(10, 1L, 2024);

        assertThat(popular).hasSize(1);
        assertThat(popular.iterator().next().getName()).isEqualTo("Needed Film");
    }

    private Film createTestFilm() {
        return createTestFilm("Test Film", "Test Description", LocalDate.of(2024, 1, 1));
    }

    private Film createTestFilm(String name, String description, LocalDate releaseDate) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(120);
        film.setMpa(new Mpa(1L, "G", null));
        return film;
    }

    private Film createAndSaveTestFilm(String name, String description, LocalDate releaseDate) {
        Film film = createTestFilm(name, description, releaseDate);
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

    @Test
    public void testGetCommonFilms() {
        User user1 = createTestUser("common1@test.com", "common1");
        User user2 = createTestUser("common2@test.com", "common2");

        Film film1 = createAndSaveTestFilm("Film 1", "Desc 1", LocalDate.of(2023, 1, 1));
        Film film2 = createAndSaveTestFilm("Film 2", "Desc 2", LocalDate.of(2023, 2, 1));
        Film film3 = createAndSaveTestFilm("Film 3", "Desc 3", LocalDate.of(2023, 3, 1));

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user1.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        Collection<Film> commonFilms = filmStorage.getCommonFilms(user1.getId(), user2.getId());

        assertThat(commonFilms).hasSize(1);
        assertThat(commonFilms.iterator().next().getName()).isEqualTo("Film 1");
    }

    @Test
    public void testGetCommonFilmsSortedByPopularity() {
        User user1 = createTestUser("userA@test.com", "userA");
        User user2 = createTestUser("userB@test.com", "userB");
        User user3 = createTestUser("userC@test.com", "userC");
        User user4 = createTestUser("userD@test.com", "userD");

        Film film1 = createAndSaveTestFilm("Popular Film", "Desc 1", LocalDate.of(2023, 1, 1));
        Film film2 = createAndSaveTestFilm("Less Popular Film", "Desc 2", LocalDate.of(2023, 2, 1));
        Film film3 = createAndSaveTestFilm("Least Popular Film", "Desc 3", LocalDate.of(2023, 3, 1));

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film1.getId(), user2.getId());
        likeStorage.addLike(film1.getId(), user3.getId());
        likeStorage.addLike(film1.getId(), user4.getId());

        likeStorage.addLike(film2.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user2.getId());
        likeStorage.addLike(film2.getId(), user3.getId());

        likeStorage.addLike(film3.getId(), user1.getId());
        likeStorage.addLike(film3.getId(), user2.getId());

        Collection<Film> commonFilms = filmStorage.getCommonFilms(user1.getId(), user2.getId());

        assertThat(commonFilms).hasSize(3);
        List<Film> filmList = new ArrayList<>(commonFilms);
        assertThat(filmList.get(0).getName()).isEqualTo("Popular Film");
        assertThat(filmList.get(1).getName()).isEqualTo("Less Popular Film");
        assertThat(filmList.get(2).getName()).isEqualTo("Least Popular Film");
    }

    @Test
    public void testGetCommonFilmsEmptyResult() {
        User user1 = createTestUser("userX@test.com", "userX");
        User user2 = createTestUser("userY@test.com", "userY");

        Film film1 = createAndSaveTestFilm("Film X", "Desc X", LocalDate.of(2023, 1, 1));
        Film film2 = createAndSaveTestFilm("Film Y", "Desc Y", LocalDate.of(2023, 2, 1));

        likeStorage.addLike(film1.getId(), user1.getId());
        likeStorage.addLike(film2.getId(), user2.getId());

        Collection<Film> commonFilms = filmStorage.getCommonFilms(user1.getId(), user2.getId());

        assertThat(commonFilms).isEmpty();
    }
}
