package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dal.EventDbStorage;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.director.DirectorService;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.genre.GenreService;
import ru.yandex.practicum.filmorate.service.mpa.MpaService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        // очищаем тестовые данные
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

        FilmService filmService = new FilmService(
                filmStorage,
                userStorage,
                mpaService,
                genreService,
                directorService,
                directorStorage,
                genreStorage,
                eventService
        );

        filmController = new FilmController(filmService);
        directorController = new DirectorController(directorService);
    }

    @Test
    public void search_ByDirector_ReturnsMatchingFilms() {
        // создаем режиссера и фильм с ним
        Director director = createTestDirector("DirectorSearchName");
        Film film = createTestFilmWithDirector("Film By Director", director.getId());

        // ищем фильмы по режиссеру
        Collection<Film> results = filmController.searchFilms("DirectorSearch", "director");

        // проверяем, что фильм найден
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(film.getName(), results.iterator().next().getName());
    }

    @Test
    public void search_ByTitleAndDirector_ReturnsCombinedResults() {
        // создаем фильм с совпадением по названию
        createTestFilm("UniqueSearchWord2024");

        // создаем фильм с совпадением по режиссеру
        Director director = createTestDirector("UniqueSearchWordDir");
        createTestFilmWithDirector("Another Film", director.getId());

        // ищем и по названию, и по режиссеру
        Collection<Film> results = filmController.searchFilms("UniqueSearchWord", "title,director");

        // проверяем, что нашли оба фильма
        assertEquals(2, results.size());
    }

    @Test
    public void search_ByTitle_ReturnsMatchingFilms() {
        // создаем фильм с нужным названием
        Film film = createTestFilm("SearchTestTitle2024");

        // ищем фильм по названию
        Collection<Film> results = filmController.searchFilms("SearchTest", "title");

        // проверяем, что фильм найден
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(film.getName(), results.iterator().next().getName());
    }

    @Test
    public void search_EmptyResult_ReturnsEmptyList() {
        // создаем фильм с другим названием
        createTestFilm("CompletelyDifferentName");

        // ищем несуществующее совпадение
        Collection<Film> results = filmController.searchFilms("NonExistentWord12345", "title");

        // проверяем, что результат пустой
        assertTrue(results.isEmpty());
    }

    @Test
    public void search_InvalidByParameter_ThrowsValidationException() {
        // проверяем, что при неверном параметре бросается ошибка
        assertThrows(ValidationException.class, () ->
                filmController.searchFilms("test", "invalid"));
    }

    @Test
    public void getCommonFilms_ShouldReturnCommonFilms() {
        // создаем пользователей
        User user1 = createTestUser("common1@test.com", "common1");
        User user2 = createTestUser("common2@test.com", "common2");

        // создаем фильмы
        Film film1 = createTestFilmWithDuration("Common Film 1", LocalDate.of(2023, 1, 1), 120);
        Film film2 = createTestFilmWithDuration("Common Film 2", LocalDate.of(2023, 2, 1), 130);
        Film film3 = createTestFilmWithDuration("Not Common Film", LocalDate.of(2023, 3, 1), 140);

        // добавляем лайки
        addLike(film1.getId(), user1.getId());
        addLike(film1.getId(), user2.getId());
        addLike(film2.getId(), user1.getId());
        addLike(film2.getId(), user2.getId());
        addLike(film3.getId(), user1.getId());

        // получаем общие фильмы
        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        // проверяем результат
        assertFalse(commonFilms.isEmpty());
        assertEquals(2, commonFilms.size());
        assertThat(commonFilms).extracting(Film::getName)
                .containsExactlyInAnyOrder("Common Film 1", "Common Film 2");
    }

    @Test
    public void getCommonFilms_ShouldReturnEmptyList_WhenNoCommonFilms() {
        User user1 = createTestUser("noCommon1@test.com", "noCommon1");
        User user2 = createTestUser("noCommon2@test.com", "noCommon2");

        Film film1 = createTestFilmWithDuration("Film A", LocalDate.of(2023, 1, 1), 120);
        Film film2 = createTestFilmWithDuration("Film B", LocalDate.of(2023, 2, 1), 130);

        addLike(film1.getId(), user1.getId());
        addLike(film2.getId(), user2.getId());

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
    public void getCommonFilms_ShouldReturnFilmsSortedByPopularity() {
        // создаем пользователей
        User user1 = createTestUser("sort1@test.com", "sort1");
        User user2 = createTestUser("sort2@test.com", "sort2");
        User user3 = createTestUser("sort3@test.com", "sort3");
        User user4 = createTestUser("extra1@test.com", "extra1");
        User user5 = createTestUser("extra2@test.com", "extra2");

        // создаем фильмы
        Film film1 = createTestFilmWithDuration("Most Popular", LocalDate.of(2023, 1, 1), 120);
        Film film2 = createTestFilmWithDuration("Medium Popular", LocalDate.of(2023, 2, 1), 130);
        Film film3 = createTestFilmWithDuration("Least Popular", LocalDate.of(2023, 3, 1), 140);

        // film1: 5 лайков (оба пользователя + 3 дополнительных)
        addLike(film1.getId(), user1.getId());
        addLike(film1.getId(), user2.getId());
        addLike(film1.getId(), user3.getId());
        addLike(film1.getId(), user4.getId());
        addLike(film1.getId(), user5.getId());

        // film2: 3 лайка (оба пользователя + 1 дополнительный)
        addLike(film2.getId(), user1.getId());
        addLike(film2.getId(), user2.getId());
        addLike(film2.getId(), user3.getId());

        // film3: 2 лайка (только оба пользователя)
        addLike(film3.getId(), user1.getId());
        addLike(film3.getId(), user2.getId());

        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        // проверяем сортировку по популярности (по убыванию)
        assertEquals(3, commonFilms.size());
        List<Film> filmList = commonFilms.stream().toList();
        assertEquals("Most Popular", filmList.get(0).getName());
        assertEquals("Medium Popular", filmList.get(1).getName());
        assertEquals("Least Popular", filmList.get(2).getName());
    }

    @Test
    public void getCommonFilms_ShouldWorkWithGenresAndDirectors() {
        // создаем пользователей
        User user1 = createTestUser("userG@test.com", "userG");
        User user2 = createTestUser("userH@test.com", "userH");

        // создаем режиссера
        Director director = createTestDirector("Test Director for Common Film");

        // создаем фильм
        Film film = createTestFilmWithDuration("Film With Genre And Director", LocalDate.of(2023, 1, 1), 120);

        // добавляем жанр (предполагаем, что genre_id=1 существует в БД)
        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), 1L);

        // добавляем режиссера
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                            film.getId(), director.getId());

        // добавляем лайки
        addLike(film.getId(), user1.getId());
        addLike(film.getId(), user2.getId());

        // получаем общие фильмы
        Collection<Film> commonFilms = filmController.getCommonFilms(user1.getId(), user2.getId());

        // проверяем, что фильм найден и содержит жанры и режиссёров
        assertFalse(commonFilms.isEmpty());
        assertEquals(1, commonFilms.size());
        Film foundFilm = commonFilms.iterator().next();

        assertFalse(foundFilm.getGenres().isEmpty());
        assertFalse(foundFilm.getDirectors().isEmpty());
        assertEquals(director.getName(), foundFilm.getDirectors().iterator().next().getName());
    }

    // создаем тестовый фильм с режиссером
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

    // создаем тестовый фильм без режиссера
    private Film createTestFilm(String name) {
        return createTestFilmWithDirector(name, null);
    }

    // создаем тестового режиссера
    private Director createTestDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorController.createDirector(director);
    }

    // очищаем таблицы перед тестами
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
