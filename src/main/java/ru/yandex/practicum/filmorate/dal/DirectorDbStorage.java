package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.*;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director> implements DirectorStorage {

    private static final String FIND_ALL = "SELECT director_id, name FROM directors ORDER BY director_id";
    private static final String FIND_BY_ID = "SELECT director_id, name FROM directors WHERE director_id = ?";
    private static final String INSERT = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE = "DELETE FROM directors WHERE director_id = ?";
    private static final String EXISTS = "SELECT EXISTS(SELECT 1 FROM directors WHERE director_id = ?)";

    private static final String INSERT_FILM_DIRECTOR =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS =
            "DELETE FROM film_directors WHERE film_id = ?";
    private static final String FIND_DIRECTORS_BY_FILM = """
        SELECT d.director_id, d.name
        FROM film_directors fd
        JOIN directors d ON fd.director_id = d.director_id
        WHERE fd.film_id = ?
        """;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate, DirectorRowMapper directorRowMapper) {
        super(jdbcTemplate, directorRowMapper);
    }

    @Override
    public Collection<Director> findAll() {
        return queryForList(FIND_ALL);
    }

    @Override
    public Optional<Director> findById(Long id) {
        return findOptional(FIND_BY_ID, id);
    }

    @Override
    public Director create(Director director) {
        Long id = insertAndGetId(INSERT, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        executeUpdate(UPDATE, director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(Long id) {
        executeUpdate(DELETE, id);
    }

    @Override
    public boolean exists(Long id) {
        return exists(EXISTS, id);
    }

    @Override
    public void setDirectors(Long filmId, Set<Long> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) return;

        List<Object[]> batchArgs = directorIds.stream()
                .map(directorId -> new Object[]{filmId, directorId})
                .toList();
        jdbcTemplate.batchUpdate(INSERT_FILM_DIRECTOR, batchArgs);
    }

    @Override
    public void updateFilmDirectors(Long filmId) {
        executeUpdate(DELETE_FILM_DIRECTORS, filmId);
    }

    @Override
    public Set<Director> getDirectorsByFilmId(Long filmId) {
        List<Director> directors = jdbcTemplate.query(FIND_DIRECTORS_BY_FILM, rowMapper, filmId);
        return new HashSet<>(directors);
    }
}
