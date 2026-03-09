package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String SELECT_FILM = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            """;

    private static final String FIND_BY_ID = SELECT_FILM + " WHERE f.film_id = ?";
    private static final String FIND_ALL = SELECT_FILM + " ORDER BY f.film_id";
    private static final String FIND_POPULAR = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name,
                   COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            GROUP BY f.film_id, mr.name
            ORDER BY likes_count DESC, f.film_id
            LIMIT ?
            """;

    private static final String INSERT = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE films SET
            name = ?,
            description = ?,
            release_date = ?,
            duration = ?,
            mpa_rating_id = ?
            WHERE film_id = ?
            """;

    private static final String DELETE = "DELETE FROM films WHERE film_id = ?";
    private static final String EXISTS = "SELECT EXISTS(SELECT 1 FROM films WHERE film_id = ?)";

    private static final String ADD_LIKE = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private final GenreStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper, GenreStorage genreStorage) {
        super(jdbcTemplate, filmRowMapper);
        this.genreStorage = genreStorage;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = queryForList(FIND_ALL);
        loadGenresForFilms(films);
        return films;
    }

    @Override
    public Film create(Film film) {
        Long id = insertAndGetId(INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = extractGenreIds(film.getGenres());
            genreStorage.setGenres(id, genreIds);
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        executeUpdate(UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        genreStorage.updateFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = extractGenreIds(film.getGenres());
            genreStorage.setGenres(film.getId(), genreIds);
        }

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> filmOpt = findOptional(FIND_BY_ID, id);
        filmOpt.ifPresent(this::loadGenres);
        return filmOpt;
    }

    @Override
    public Film delete(Long id) {
        Film film = findById(id).orElseThrow();
        executeUpdate(DELETE, id);
        return film;
    }

    @Override
    public Collection<Film> getPopular(int count) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR, rowMapper, count);
        loadGenresForFilms(films);
        return films;
    }

    @Override
    public Long addLike(Long filmId, Long userId) {
        executeUpdate(ADD_LIKE, filmId, userId);
        return filmId;
    }

    @Override
    public Long removeLike(Long filmId, Long userId) {
        executeUpdate(REMOVE_LIKE, filmId, userId);
        return filmId;
    }

    @Override
    public boolean contains(Long id) {
        return exists(EXISTS, id);
    }

    private void loadGenres(Film film) {
        Set<Genre> genres = genreStorage.getGenresByFilmId(film.getId());
        film.setGenres(genres);
    }

    private void loadGenresForFilms(List<Film> films) {
        for (Film film : films) {
            loadGenres(film);
        }
    }

    private Set<Long> extractGenreIds(Set<Genre> genres) {
        return genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
    }
}
