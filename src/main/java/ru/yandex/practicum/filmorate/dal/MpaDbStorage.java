package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {

    private static final String FIND_ALL = "SELECT rating_id, name, description FROM mpa_rating ORDER BY rating_id";
    private static final String FIND_BY_ID = "SELECT rating_id, name, description FROM mpa_rating WHERE rating_id = ?";

    public MpaDbStorage(JdbcTemplate jdbcTemplate, MpaRowMapper mpaRowMapper) {
        super(jdbcTemplate, mpaRowMapper);
    }

    @Override
    public Collection<Mpa> findAll() {
        return queryForList(FIND_ALL);
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        return findOptional(FIND_BY_ID, id);
    }
}