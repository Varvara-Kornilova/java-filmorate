package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String SELECT_FILM = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            """;

    private static final String FIND_BY_ID = SELECT_FILM + " WHERE f.film_id = ?";
    private static final String FIND_ALL = SELECT_FILM + " ORDER BY f.film_id";

    private static final String FIND_BY_DIRECTOR_SORT_LIKES = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                   COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE fd.director_id = ?
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                     f.mpa_rating_id, mr.name, mr.description
            ORDER BY likes_count DESC, f.film_id
            """;

    private static final String FIND_BY_DIRECTOR_SORT_YEAR = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            JOIN film_directors fd ON f.film_id = fd.film_id
            WHERE fd.director_id = ?
            ORDER BY f.release_date, f.film_id
            """;

    private static final String SEARCH_BY_TITLE = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                   COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            WHERE LOWER(f.name) LIKE LOWER(?)
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                     f.mpa_rating_id, mr.name, mr.description
            ORDER BY likes_count DESC, f.film_id
            """;

    private static final String SEARCH_BY_DIRECTOR = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                   COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            JOIN film_directors fd ON f.film_id = fd.film_id
            JOIN directors d ON fd.director_id = d.director_id
            WHERE LOWER(d.name) LIKE LOWER(?)
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                     f.mpa_rating_id, mr.name, mr.description
            ORDER BY likes_count DESC, f.film_id
            """;

    private static final String SEARCH_BY_TITLE_AND_DIRECTOR = """
            SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                   COUNT(DISTINCT l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
            LEFT JOIN likes l ON f.film_id = l.film_id
            LEFT JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.director_id
            WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)
            GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                     f.mpa_rating_id, mr.name, mr.description
            ORDER BY likes_count DESC, f.film_id
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
    private final DirectorStorage directorStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper,
                         GenreStorage genreStorage, DirectorStorage directorStorage) {
        super(jdbcTemplate, filmRowMapper);
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    @Override
    public Collection<Film> findByDirectorId(Long directorId, String sortBy) {
        String sql = "likes".equalsIgnoreCase(sortBy)
                ? FIND_BY_DIRECTOR_SORT_LIKES
                : FIND_BY_DIRECTOR_SORT_YEAR;

        List<Film> films = jdbcTemplate.query(sql, rowMapper, directorId);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        loadLikesForFilms(films);
        return films;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = queryForList(FIND_ALL);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        loadLikesForFilms(films);
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
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        Optional<Film> filmOpt = findOptional(FIND_BY_ID, id);
        filmOpt.ifPresent(film -> {
            loadGenres(film);
            loadDirectors(film);
            loadLikes(film);
        });
        return filmOpt;
    }

    @Override
    public Film delete(Long id) {
        Film film = findById(id).orElseThrow();
        executeUpdate(DELETE, id);
        return film;
    }

    @Override
    public Collection<Film> getPopular(int count, Long genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration,
                       f.mpa_rating_id, mr.name AS mpa_name, mr.description AS mpa_description,
                       COUNT(DISTINCT l.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa_rating mr ON f.mpa_rating_id = mr.rating_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                """);

        List<Object> params = new ArrayList<>();

        // Добавляем связь с жанрами только при фильтрации по жанру
        if (genreId != null) {
            sql.append("JOIN film_genres fg ON f.film_id = fg.film_id ");
        }

        sql.append("WHERE 1 = 1 ");

        // Фильтруем по жанру
        if (genreId != null) {
            sql.append("AND fg.genre_id = ? ");
            params.add(genreId);
        }

        // Фильтруем по году
        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }

        sql.append("""
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration,
                         f.mpa_rating_id, mr.name, mr.description
                ORDER BY likes_count DESC, f.film_id
                LIMIT ?
                """);

        params.add(count);

        List<Film> films = jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        loadLikesForFilms(films);
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

    @Override
    public Collection<Film> search(String query, String by) {
        String searchPattern = "%" + query + "%";
        List<Film> films;

        if (by == null || by.isBlank()) {
            return List.of();
        }

        String[] searchBy = by.toLowerCase().split(",");
        boolean searchByTitle = false;
        boolean searchByDirector = false;

        for (String s : searchBy) {
            String trimmed = s.trim();
            if ("title".equals(trimmed)) {
                searchByTitle = true;
            } else if ("director".equals(trimmed)) {
                searchByDirector = true;
            }
        }

        if (searchByTitle && searchByDirector) {
            films = jdbcTemplate.query(SEARCH_BY_TITLE_AND_DIRECTOR, rowMapper, searchPattern, searchPattern);
        } else if (searchByTitle) {
            films = jdbcTemplate.query(SEARCH_BY_TITLE, rowMapper, searchPattern);
        } else if (searchByDirector) {
            films = jdbcTemplate.query(SEARCH_BY_DIRECTOR, rowMapper, searchPattern);
        } else {
            return List.of();
        }

        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        loadLikesForFilms(films);

        return films;
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

    private void loadDirectors(Film film) {
        Set<Director> directors = directorStorage.getDirectorsByFilmId(film.getId());
        film.setDirectors(directors);
    }

    private void loadDirectorsForFilms(List<Film> films) {
        for (Film film : films) {
            loadDirectors(film);
        }
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likeUserIds = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likeUserIds));
    }

    private void loadLikesForFilms(List<Film> films) {
        for (Film film : films) {
            loadLikes(film);
        }
    }
}