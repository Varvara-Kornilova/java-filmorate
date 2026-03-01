package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getObject("release_date", java.time.LocalDate.class));
        film.setDuration(rs.getInt("duration"));

        Long mpaId = rs.getObject("mpa_rating_id", Long.class);
        if (mpaId != null) {
            Mpa mpa = new Mpa(
                    mpaId,
                    rs.getString("mpa_code"),
                    rs.getString("mpa_description")
            );
            film.setMpa(mpa);
        }
        return film;
    }
}
