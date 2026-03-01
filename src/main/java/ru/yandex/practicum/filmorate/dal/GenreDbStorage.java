package ru.yandex.practicum.filmorate.dal;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;

@Component("genreDbStorage")
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {

    public GenreDbStorage(JdbcTemplate jdbc) {
        super(jdbc, (rs, rowNum) -> new Genre(
                rs.getLong("genre_id"),
                rs.getString("name")
        ));
    }

    @Override
    public List<Genre> findAll() {
        return getAll("SELECT genre_id, name FROM genres ORDER BY genre_id");
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return get("SELECT genre_id, name FROM genres WHERE genre_id = ?", id);
    }
}
