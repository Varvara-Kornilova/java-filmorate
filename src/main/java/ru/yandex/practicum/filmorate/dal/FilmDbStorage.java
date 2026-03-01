package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    public FilmDbStorage(JdbcTemplate jdbc) {
        super(jdbc, new FilmRowMapper());
    }


    @Override
    public List<Film> findAllFilms() {
        String query = """
        SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
               m.rating_id AS mpa_rating_id, m.code AS mpa_code, m.description AS mpa_description
        FROM films f
        LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.rating_id
        """;

        List<Film> films = getAll(query);

        for (Film film : films) {
            film.setLikes(loadLikes(film.getId()));

            Set<Genre> genres = loadGenres(film.getId());
            film.setGenres(genres);
            film.setGenreIds(genres.stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
        }

        return films;
    }

    @Override
    public Film create(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

        Long id = insert(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpaRatingId()
        );

        film.setId(id);

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            saveFilmGenres(id, film.getGenreIds());
        }

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        Optional<Film> existing = findById(newFilm.getId());
        if (existing.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ?";
        jdbc.update(query,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpaRatingId(),
                newFilm.getId()
        );

        if (newFilm.getGenreIds() != null) {
            jdbc.update("DELETE FROM film_genres WHERE film_id = ?", newFilm.getId());
            saveFilmGenres(newFilm.getId(), newFilm.getGenreIds());
        }

        return newFilm;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String query = """
    SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id,
           m.code as mpa_code, m.description as mpa_description
    FROM films f
    LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.rating_id
    WHERE f.film_id = ?
    """;

        Optional<Film> filmOpt = get(query, id);
        if (filmOpt.isEmpty()) return Optional.empty();

        Film film = filmOpt.get();

        // ✅ Добавь эту строку:
        film.setLikes(loadLikes(film.getId()));

        Set<Genre> genres = loadGenres(film.getId());
        film.setGenres(genres);
        film.setGenreIds(genres.stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        return Optional.of(film);
    }

    private void saveFilmGenres(Long filmId, Set<Long> genreIds) {
        String query = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Long genreId : genreIds) {
            jdbc.update(query, filmId, genreId);
        }
    }

    private Set<Genre> loadGenres(Long filmId) {
        String query = """
        SELECT g.genre_id, g.name 
        FROM film_genres fg 
        JOIN genres g ON fg.genre_id = g.genre_id 
        WHERE fg.film_id = ?
        """;

        List<Genre> genreList = jdbc.query(query,
                (rs, rowNum) -> {
                    Long genreId = rs.getObject("genre_id", Long.class);  // ✅ Безопасное получение
                    String name = rs.getString("name");
                    return new Genre(genreId, name);
                },
                filmId);

        return genreList.stream()
                .filter(g -> g != null && g.getId() != null)
                .collect(Collectors.toSet());
    }

    private Set<Long> loadLikes(Long filmId) {
        String query = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likeUserIds = jdbc.queryForList(query, Long.class, filmId);
        return new HashSet<>(likeUserIds);
    }


}
