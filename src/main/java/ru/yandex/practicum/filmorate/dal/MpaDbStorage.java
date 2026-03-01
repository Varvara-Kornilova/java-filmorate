package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;

@Component("mpaDbStorage")
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {

    public MpaDbStorage(JdbcTemplate jdbc) {
        super(jdbc, (rs, rowNum) -> new Mpa(
                rs.getLong("rating_id"),
                rs.getString("code"),
                rs.getString("description")
        ));
    }

    @Override
    public List<Mpa> findAll() {
        return getAll("SELECT rating_id, code, description FROM mpa_rating ORDER BY rating_id");
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        return get("SELECT rating_id, code, description FROM mpa_rating WHERE rating_id = ?", id);
    }
}
