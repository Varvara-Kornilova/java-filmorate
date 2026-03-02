package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));

        LocalDate releaseDate = rs.getObject("release_date", LocalDate.class);
        film.setReleaseDate(releaseDate);

        extractMpaRating(film, rs);

        film.setGenres(new HashSet<>());

        return film;
    }

    private void extractMpaRating(Film film, ResultSet rs) throws SQLException {
        long mpaId = rs.getLong("mpa_rating_id");

        if (!rs.wasNull()) {
            String mpaName = rs.getString("mpa_name");
            film.setMpa(Mpa.of(mpaId, mpaName));
        }
    }
}
