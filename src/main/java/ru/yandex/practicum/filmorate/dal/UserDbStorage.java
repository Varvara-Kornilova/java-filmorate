package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String FIND_ALL = "SELECT * FROM users ORDER BY user_id";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT = """
            INSERT INTO users (email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE = """
            UPDATE users SET
            email = ?,
            login = ?,
            name = ?,
            birthday = ?
            WHERE user_id = ?
            """;
    private static final String DELETE = "DELETE FROM users WHERE user_id = ?";
    private static final String EXISTS = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?)";

    private static final String GET_RECOMMENDATIONS = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                   COUNT(DISTINCT l2.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            LEFT JOIN likes l2 ON f.film_id = l2.film_id
            WHERE f.film_id IN (
                SELECT DISTINCT l.film_id
                FROM likes l
                WHERE l.user_id = (
                    SELECT other_user_id FROM (
                        SELECT
                            l.user_id AS other_user_id,
                            COUNT(l.film_id) AS overlap_count
                        FROM likes l
                        WHERE l.user_id != ?
                          AND l.film_id IN (SELECT film_id FROM likes WHERE user_id = ?)
                        GROUP BY l.user_id
                        HAVING COUNT(l.film_id) > 0
                        ORDER BY overlap_count DESC
                        LIMIT 1
                    )
                )
                AND l.film_id NOT IN (SELECT film_id FROM likes WHERE user_id = ?)
            )
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                     f.mpa_rating_id, mr.name, mr.description
            ORDER BY likes_count DESC, f.film_id
            """;

    private final FilmRowMapper filmRowMapper;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public UserDbStorage(JdbcTemplate jdbcTemplate,
                         UserRowMapper userRowMapper,
                         FilmRowMapper filmRowMapper,
                         GenreStorage genreStorage,
                         DirectorStorage directorStorage) {
        super(jdbcTemplate, userRowMapper);
        this.filmRowMapper = filmRowMapper;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    @Override
    public Collection<User> findAll() {
        return queryForList(FIND_ALL);
    }

    @Override
    public User create(User user) {
        Long id = insertAndGetId(INSERT,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        executeUpdate(UPDATE,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOptional(FIND_BY_ID, id);
    }

    @Override
    public void delete(Long id) {
        executeUpdate(DELETE, id);
    }

    @Override
    public boolean contains(Long id) {
        return exists(EXISTS, id);
    }

    @Override
    public Collection<Film> getRecommendations(Long userId) {

        Set<Long> userLikedFilms = new HashSet<>(jdbcTemplate.queryForList(
            "SELECT film_id FROM likes WHERE user_id = ?", Long.class, userId));

        if (userLikedFilms.isEmpty()) {
            return List.of();
        }

        String findSimilarUserSql = """
            SELECT l.user_id
            FROM likes l
            WHERE l.user_id != ?
              AND l.film_id IN (SELECT film_id FROM likes WHERE user_id = ?)
            GROUP BY l.user_id
            ORDER BY COUNT(l.film_id) DESC
            LIMIT 1
            """;

        List<Long> similarUsers = jdbcTemplate.queryForList(
            findSimilarUserSql, Long.class, userId, userId);

        if (similarUsers.isEmpty()) {
            return List.of();
        }

        Long similarUserId = similarUsers.get(0);

        Set<Long> similarUserFilms = new HashSet<>(jdbcTemplate.queryForList(
            "SELECT film_id FROM likes WHERE user_id = ?", Long.class, similarUserId));

        similarUserFilms.removeAll(userLikedFilms);

        if (similarUserFilms.isEmpty()) {
            return List.of();
        }

        String placeholders = similarUserFilms.stream()
            .map(f -> "?")
            .collect(Collectors.joining(","));

        String getFilmsSql = String.format("""
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                   COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE f.film_id IN (%s)
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                     f.mpa_rating_id, mr.name, mr.description
            ORDER BY likes_count DESC, f.film_id
            """, placeholders);

        List<Film> films = jdbcTemplate.query(
            getFilmsSql,
            filmRowMapper,
            similarUserFilms.toArray());

        for (Film film : films) {
            loadGenresForFilm(film);
            loadDirectorsForFilm(film);
            loadLikesForFilm(film);
        }

        return films;
    }

    private void loadGenresForFilm(Film film) {
        try {
            Set<Genre> genres = genreStorage.getGenresByFilmId(film.getId());
            film.setGenres(genres != null ? genres : new HashSet<>());
        } catch (Exception e) {
            film.setGenres(new HashSet<>());
        }
    }

    private void loadDirectorsForFilm(Film film) {
        try {
            Set<Director> directors = directorStorage.getDirectorsByFilmId(film.getId());
            film.setDirectors(directors != null ? directors : new HashSet<>());
        } catch (Exception e) {
            film.setDirectors(new HashSet<>());
        }
    }

    private void loadLikesForFilm(Film film) {
        List<Long> likeUserIds = jdbcTemplate.queryForList(
            "SELECT user_id FROM likes WHERE film_id = ?", Long.class, film.getId());
        film.setLikes(new HashSet<>(likeUserIds));
    }
}
