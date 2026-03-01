package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = """
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                       m.rating_id AS mpa_rating_id, m.code AS mpa_code, m.description AS mpa_description
                FROM films f
                LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.rating_id
                """;
        return jdbcTemplate.query(sql, new FilmRowMapper());
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa() != null ? newFilm.getMpa().getId() : null,
                newFilm.getId());
        return newFilm;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = """
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                       m.rating_id AS mpa_rating_id, m.code AS mpa_code, m.description AS mpa_description
                FROM films f
                LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.rating_id
                WHERE f.film_id = ?
                """;
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);
        return films.isEmpty() ? Optional.empty() : Optional.of(films.getFirst());
    }
}
