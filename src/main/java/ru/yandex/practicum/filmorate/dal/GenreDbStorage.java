package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.*;

/**
 * Хранилище для работы с жанрами фильмов.
 */
@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {

    private static final String FIND_ALL = "SELECT genre_id, name FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
    private static final String INSERT_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM = """
            SELECT g.genre_id, g.name 
            FROM film_genres fg 
            JOIN genres g ON fg.genre_id = g.genre_id 
            WHERE fg.film_id = ?
            """;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
        super(jdbcTemplate, genreRowMapper);
    }

    @Override
    public Collection<Genre> findAll() {
        return queryForList(FIND_ALL);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return findOptional(FIND_BY_ID, id);
    }

    @Override
    public void setGenres(Long filmId, Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        List<Object[]> batchArgs = genreIds.stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .toList();
        jdbcTemplate.batchUpdate(INSERT_GENRE, batchArgs);
    }

    @Override
    public void updateFilmGenres(Long filmId) {
        // Удаляем старые связи перед обновлением (вызывается из Service)
        executeUpdate(DELETE_GENRES, filmId);
    }

    @Override
    public Set<Genre> getGenresByFilmId(Long filmId) {
        List<Genre> genres = jdbcTemplate.query(FIND_GENRES_BY_FILM, rowMapper, filmId);
        return new HashSet<>(genres);
    }
}
